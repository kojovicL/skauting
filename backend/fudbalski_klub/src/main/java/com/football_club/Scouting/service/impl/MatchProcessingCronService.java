package com.football_club.Scouting.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.football_club.Clients.APIFootballClient;
import com.football_club.Scouting.model.*;
import com.football_club.Scouting.model.League;
import com.football_club.Scouting.model.Match;
import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.repository.*;
import com.football_club.Scouting.service.DateTimeService;
import com.football_club.dto.apifootball.fixtures.FixturesResponse;
import com.football_club.dto.apifootball.fixtures.Response;
import com.football_club.dto.apifootball.playerstats.PlayerStats;
import com.football_club.dto.apifootball.playerstats.Statistic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchProcessingCronService {

    private final MonitoredPlayerRepository monitoredPlayerRepository;
    private final APIFootballClient apiClient;
    private final MatchRepository matchRepository;
    private final ApiMatchStatRepository apiMatchStatRepository;
    private final ApiMatchValuedMetricRepository apiMatchValuedMetricRepository;
    private final MetricRepository metricRepository;
    private final NotificationRepository notificationRepository;
    private final DateTimeService dateTimeService;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final SeasonRepository seasonRepository;

    @Scheduled(cron = "0 0 2 * * *") // Runs every day at 2 AM
    public void processDailyMatches() {
        log.info("Starting daily match processing cron job...");
        LocalDate yesterday = dateTimeService.getYesterday();
        LocalDate nextWeek = dateTimeService.getInSevenDays();
        Integer currentSeason = dateTimeService.getCurrentSeasonYear();

        // 1. Fetch active monitored players and group by team to minimize API requests
        List<MonitoredPlayer> activePlayers = monitoredPlayerRepository.findAllActiveMonitoredPlayers();
        Map<Long, List<MonitoredPlayer>> playersByTeam = activePlayers.stream()
                .collect(Collectors.groupingBy(MonitoredPlayer::getTeamId));

        for (Map.Entry<Long, List<MonitoredPlayer>> entry : playersByTeam.entrySet()) {
            Long teamId = entry.getKey();
            List<MonitoredPlayer> teamPlayers = entry.getValue();

            try {
                processTeamFixtures(teamId, teamPlayers, yesterday, nextWeek, currentSeason);
            } catch (Exception e) {
                log.error("Error processing fixtures for team {}: {}", teamId, e.getMessage());
            }
        }
        log.info("Daily match processing completed.");
    }

    @Transactional
    protected void processTeamFixtures(Long teamId, List<MonitoredPlayer> teamPlayers, LocalDate yesterday, LocalDate nextWeek, Integer currentSeason) {
        FixturesResponse fixturesResponse = apiClient.getTeamFixtures(teamId, yesterday, nextWeek, currentSeason);

        if (fixturesResponse.getResponse() == null || fixturesResponse.getResponse().isEmpty()) {
            return;
        }

        for (Response fixtureData : fixturesResponse.getResponse()) {
            LocalDateTime matchDateTime = OffsetDateTime.parse(fixtureData.getFixture().getDate()).toLocalDateTime();
            LocalDate matchDate = matchDateTime.toLocalDate();

            Match match = resolveMatch(fixtureData, matchDateTime, matchDate, yesterday);

            // If the match was exactly yesterday, extract deep player statistics
            if (matchDate.isEqual(yesterday)) {
                extractAndSavePlayerStats(fixtureData.getFixture().getId().longValue(), teamId, teamPlayers, match);
            }
        }
    }

    private Match resolveMatch(Response fixtureData, LocalDateTime matchDateTime, LocalDate matchDate, LocalDate yesterday) {
        Long fixtureId = fixtureData.getFixture().getId().longValue();
        Match match = matchRepository.findById(fixtureId).orElseGet(Match::new);

        match.setId(fixtureId);
        match.setMatchDate(matchDateTime);

        // Force future matches to SCHEDULED. If YESTERDAY, keep actual API status (e.g., FT, PEN)
        if (matchDate.isAfter(yesterday)) {
            match.setStatus("SCHEDULED");
        } else {
            match.setStatus(fixtureData.getFixture().getStatus().getShort());
        }

        match.setHomeGoals(fixtureData.getGoals().getHome());
        match.setAwayGoals(fixtureData.getGoals().getAway());

        League league = resolveLeague(fixtureData.getLeague());
        match.setLeague(league);
        match.setSeason(resolveSeason(league, fixtureData.getLeague().getSeason()));
        match.setHomeTeam(resolveTeam(fixtureData.getTeams().getHome().getId().longValue(), fixtureData.getTeams().getHome().getName(), fixtureData.getTeams().getHome().getLogo(), league));
        match.setAwayTeam(resolveTeam(fixtureData.getTeams().getAway().getId().longValue(), fixtureData.getTeams().getAway().getName(), fixtureData.getTeams().getAway().getLogo(), league));

        return matchRepository.save(match);
    }

    private void extractAndSavePlayerStats(Long fixtureId, Long teamId, List<MonitoredPlayer> teamPlayers, Match match) {
        PlayerStats statsResponse = apiClient.getFixturePlayerStats(fixtureId, teamId);

        if (statsResponse.getResponse() == null || statsResponse.getResponse().isEmpty()) {
            return;
        }

        List<com.football_club.dto.apifootball.playerstats.Player> apiPlayers = statsResponse.getResponse().get(0).getPlayers();

        // Load metric definitions once per match extraction
        Map<String, Metric> metricMap = metricRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Metric::getName,
                        m -> m,
                        (existing, replacement) -> existing
                ));

        for (MonitoredPlayer mp : teamPlayers) {
            apiPlayers.stream()
                    .filter(ap -> ap.getPlayer().getId().longValue() == mp.getPlayer().getId())
                    .findFirst()
                    .ifPresent(apiPlayer -> {
                        if (!apiPlayer.getStatistics().isEmpty()) {
                            saveApiMatchStat(apiPlayer.getStatistics().get(0), mp, match, metricMap);
                            createScoutNotification(mp, match);
                        }
                    });
        }
    }

    private void saveApiMatchStat(Statistic stat, MonitoredPlayer mp, Match match, Map<String, Metric> metricMap) {
        if (apiMatchStatRepository.existsByPlayerIdAndMatchId(mp.getPlayer().getId(), match.getId())) {
            return;
        }

        Double rawRating = parseDoubleSafely(stat.getGames().getRating());
        Double weightedRating = (rawRating != null ? rawRating : 0.0) * match.getLeague().getDifficultyMultiplier();

        Integer goals = stat.getGoals() != null ? (Integer) parseIntegerSafely(stat.getGoals().getTotal()) : 0;
        Integer assists = stat.getGoals() != null ? (Integer) parseIntegerSafely(stat.getGoals().getAssists()) : 0;

        ApiMatchStat matchStat = ApiMatchStat.builder()
                .player(mp.getPlayer())
                .match(match)
                .minutesPlayed(stat.getGames().getMinutes())
                .shirtNumber(stat.getGames().getNumber())
                .isSubstitute(stat.getGames().getSubstitute())
                .isCaptain(stat.getGames().getCaptain())
                .goals(goals)
                .assists(assists)
                .rawRating(rawRating != null ? rawRating : 0.0)
                .weightedRating(weightedRating)
                .build();

        ApiMatchStat savedStat = apiMatchStatRepository.save(matchStat);

        List<ApiMatchValuedMetric> valuedMetrics = new ArrayList<>();

        if (stat.getShots() != null) {
            addMetric("Shots Total", stat.getShots().getTotal(), savedStat, metricMap, valuedMetrics);
            addMetric("Shots On Target", stat.getShots().getOn(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getPasses() != null) {
            addMetric("Total Passes", stat.getPasses().getTotal(), savedStat, metricMap, valuedMetrics);
            addMetric("Key Passes", stat.getPasses().getKey(), savedStat, metricMap, valuedMetrics);
            addMetric("Pass Accuracy", parsePercentageSafely(stat.getPasses().getAccuracy()), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getTackles() != null) {
            addMetric("Tackles", stat.getTackles().getTotal(), savedStat, metricMap, valuedMetrics);
            addMetric("Blocks", stat.getTackles().getBlocks(), savedStat, metricMap, valuedMetrics);
            addMetric("Interceptions", stat.getTackles().getInterceptions(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getDribbles() != null) {
            addMetric("Dribble Attempts", stat.getDribbles().getAttempts(), savedStat, metricMap, valuedMetrics);
            addMetric("Successful Dribbles", stat.getDribbles().getSuccess(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getDuels() != null) {
            addMetric("Total Duels", stat.getDuels().getTotal(), savedStat, metricMap, valuedMetrics);
            addMetric("Duels Won", stat.getDuels().getWon(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getFouls() != null) {
            addMetric("Fouls Drawn", stat.getFouls().getDrawn(), savedStat, metricMap, valuedMetrics);
            addMetric("Fouls Committed", stat.getFouls().getCommitted(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getCards() != null) {
            addMetric("Yellow Cards", stat.getCards().getYellow(), savedStat, metricMap, valuedMetrics);
            addMetric("Red Cards", stat.getCards().getRed(), savedStat, metricMap, valuedMetrics);
        }
        if (stat.getPenalty() != null) {
            addMetric("Penalties Won", stat.getPenalty().getWon(), savedStat, metricMap, valuedMetrics);
            addMetric("Penalties Scored", stat.getPenalty().getScored(), savedStat, metricMap, valuedMetrics);
        }

        apiMatchValuedMetricRepository.saveAll(valuedMetrics);
    }

    private void addMetric(String name, Object rawValue, ApiMatchStat stat, Map<String, Metric> metricMap, List<ApiMatchValuedMetric> list) {
        Double value = parseDoubleSafely(rawValue);
        double finalValue = value != null ? value : 0.0;
        if (metricMap.containsKey(name)) {
            list.add(ApiMatchValuedMetric.builder()
                    .apiMatchStat(stat)
                    .metric(metricMap.get(name))
                    .value(finalValue)
                    .build());
        }
    }

    private void createScoutNotification(MonitoredPlayer mp, Match match) {
        if (mp.getScout() == null) {
            return;
        }

        Notification notification = Notification.builder()
                .scout(mp.getScout())
                .playerId(mp.getPlayer().getId())
                .matchId(match.getId())
                .title("Novi meč zabeležen")
                .message("Igrač " + mp.getPlayer().getName() + " " + mp.getPlayer().getSurname() +
                        " je upravo odigrao utakmicu protiv " +
                        (match.getHomeTeam().getId().equals(mp.getTeamId()) ? match.getAwayTeam().getName() : match.getHomeTeam().getName()) + ".")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    private League resolveLeague(com.football_club.dto.apifootball.fixtures.League apiLeague) {
        if (apiLeague == null || apiLeague.getId() == null) return null;
        return leagueRepository.findById(apiLeague.getId().longValue())
                .orElseGet(() -> {
                    League l = new League();
                    l.setId(apiLeague.getId().longValue());
                    l.setName(apiLeague.getName());
                    l.setDifficultyMultiplier(1.0);
                    return leagueRepository.save(l);
                });
    }

    private Team resolveTeam(Long teamId, String name, String logo, League league) {
        if (teamId == null) return null;
        return teamRepository.findById(teamId)
                .orElseGet(() -> {
                    Team t = new Team();
                    t.setId(teamId);
                    t.setName(name != null ? name : "Unknown Team");
                    t.setLogoUrl(logo);
                    t.setLeague(league);
                    return teamRepository.save(t);
                });
    }

    private Season resolveSeason(League league, Integer year) {
        if (league == null || year == null) return null;

        return seasonRepository.findByLeagueIdAndYear(league.getId(), year)
                .orElseGet(() -> {
                    LocalDate startDate = null;
                    LocalDate endDate = null;

                    try {
                        JsonNode root = apiClient.getLeagueSeasonDetails(league.getId(), year);
                        if (root != null && root.has("response") && !root.path("response").isEmpty()) {
                            JsonNode seasonsNode = root.path("response").get(0).path("seasons");
                            for (JsonNode s : seasonsNode) {
                                if (s.path("year").asInt() == year) {
                                    startDate = LocalDate.parse(s.path("start").asText());
                                    endDate = LocalDate.parse(s.path("end").asText());
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("Could not retrieve real season dates from API for league {}: {}", league.getId(), e.getMessage());
                    }

                    // Fallback: Use smart heuristic if API fails or quota is exhausted
                    if (startDate == null || endDate == null) {
                        startDate = LocalDate.of(year, 8, 1);
                        endDate = LocalDate.of(year + 1, 5, 31);
                    }

                    Season newSeason = Season.builder()
                            .league(league)
                            .year(year)
                            .startDate(startDate)
                            .endDate(endDate)
                            .build();

                    return seasonRepository.save(newSeason);
                });
    }

    private Double parseDoubleSafely(Object obj) {
        if (obj == null) return null;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseIntegerSafely(Object obj) {
        if (obj == null) return 0;
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Double parsePercentageSafely(String val) {
        if (val == null || val.isBlank()) return 0.0;
        try {
            return Double.parseDouble(val.replace("%", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}