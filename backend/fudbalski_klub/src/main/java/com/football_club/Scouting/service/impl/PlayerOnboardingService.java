package com.football_club.Scouting.service.impl;

import com.football_club.Clients.APIFootballClient;
import com.football_club.Scouting.dto.OnboardPlayerRequest;
import com.football_club.Scouting.model.*;
import com.football_club.Scouting.model.League;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.repository.*;
import com.football_club.Scouting.service.IPlayerOnboardingService;
import com.football_club.dto.apifootball.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerOnboardingService implements IPlayerOnboardingService {

    private final APIFootballClient apiClient;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final CampaignRepository campaignRepository;
    private final MonitoredPlayerRepository monitoredPlayerRepository;
    private final SeasonalReportRepository seasonalReportRepository;
    private final SeasonalValuedMetricRepository seasonalValuedMetricRepository;
    private final MetricRepository metricRepository;

    @Override
    @Transactional
    public MonitoredPlayer onboardPlayer(OnboardPlayerRequest request) {
        int targetSeason = request.getSeason() != null ? request.getSeason() : 2024;

        PlayerSearchResponse targetResponse = apiClient.getPlayerByIdAndSeason(request.getApiPlayerId(), targetSeason);
        if (targetResponse.getResponse() == null || targetResponse.getResponse().isEmpty()) {
            throw new IllegalArgumentException("Igrač nije pronađen na API-Football za datu sezonu: " + targetSeason);
        }

        Response__1 primaryData = targetResponse.getResponse().get(0);
        Player__1 apiPlayer = primaryData.getPlayer();

        if (primaryData.getStatistics() == null || primaryData.getStatistics().isEmpty()) {
            throw new IllegalArgumentException("Nisu pronađene statistike za igrača u datoj sezoni.");
        }

        // Pick the competition where the player accumulated the most playing time
        Statistic primaryStat = primaryData.getStatistics().stream()
                .filter(s -> s.getLeague() != null && s.getLeague().getId() != null)
                .max(Comparator.comparingInt(this::getMinutesPlayed))
                .orElse(primaryData.getStatistics().get(0));

        League currentLeague = resolveLeague(primaryStat.getLeague());
        Team currentTeam = resolveTeam(primaryStat.getTeam(), currentLeague);

        double difficultyMultiplier = currentLeague != null ? currentLeague.getDifficultyMultiplier() : 1.0;

        Player player = playerRepository.findById(apiPlayer.getId().longValue()).orElseGet(Player::new);
        player.setId(apiPlayer.getId().longValue());
        player.setName(apiPlayer.getFirstname() != null ? apiPlayer.getFirstname() : apiPlayer.getName());
        player.setSurname(apiPlayer.getLastname() != null ? apiPlayer.getLastname() : "");
        player.setAge(apiPlayer.getAge());
        player.setNationality(apiPlayer.getNationality());
        player.setPhotoUrl(apiPlayer.getPhoto());
        if (primaryStat.getGames() != null && primaryStat.getGames().getPosition() != null) {
            player.setPosition(Position.fromApiString(primaryStat.getGames().getPosition()));
        }
        player.setCurrentTeam(currentTeam);
        Player savedPlayer = playerRepository.save(player);

        createHistoricalSeasonalReport(savedPlayer, targetSeason, primaryStat, difficultyMultiplier);

        backfillPreviousSeasons(savedPlayer, targetSeason, 2);

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Kampanja ne postoji."));

        MonitoredPlayer mp = new MonitoredPlayer();
        mp.setPlayer(savedPlayer);
        mp.setCampaign(campaign);
        mp.setTeamId(currentTeam != null ? currentTeam.getId() : 0L);
        mp.setAddedAt(LocalDate.now());

        return monitoredPlayerRepository.save(mp);
    }

    private void backfillPreviousSeasons(Player player, int currentSeason, int seasonsToBackfill) {
        for (int i = 1; i <= seasonsToBackfill; i++) {
            int pastSeason = currentSeason - i;

            if (seasonalReportRepository.existsByPlayerIdAndSeasonYear(player.getId(), pastSeason)) {
                continue;
            }

            try {
                PlayerSearchResponse pastResponse = apiClient.getPlayerByIdAndSeason(player.getId(), pastSeason);
                if (pastResponse.getResponse() != null && !pastResponse.getResponse().isEmpty()) {
                    List<Statistic> stats = pastResponse.getResponse().get(0).getStatistics();
                    if (stats != null && !stats.isEmpty()) {
                        // Pick competition with highest minutes for each historical season as well
                        Statistic pastStat = stats.stream()
                                .filter(s -> s.getLeague() != null && s.getLeague().getId() != null)
                                .max(Comparator.comparingInt(this::getMinutesPlayed))
                                .orElse(stats.get(0));

                        League pastLeague = resolveLeague(pastStat.getLeague());
                        double pastMultiplier = pastLeague != null ? pastLeague.getDifficultyMultiplier() : 1.0;

                        createHistoricalSeasonalReport(player, pastSeason, pastStat, pastMultiplier);
                    }
                }
            } catch (Exception e) {
                log.warn("Nisu pronađeni podaci za igrača {} za sezonu {}: {}", player.getId(), pastSeason, e.getMessage());
            }
        }
    }

    private int getMinutesPlayed(Statistic stat) {
        return (stat.getGames() != null && stat.getGames().getMinutes() != null)
                ? stat.getGames().getMinutes()
                : 0;
    }

    private League resolveLeague(com.football_club.dto.apifootball.League apiLeague) {
        if (apiLeague == null || apiLeague.getId() == null) return null;
        return leagueRepository.findById(apiLeague.getId().longValue())
                .orElseGet(() -> {
                    League l = new League();
                    l.setId(apiLeague.getId().longValue());
                    l.setName(apiLeague.getName() != null ? apiLeague.getName() : "Unknown League");
                    l.setDifficultyMultiplier(1.0);
                    return leagueRepository.save(l);
                });
    }

    private Team resolveTeam(com.football_club.dto.apifootball.Team apiTeam, League league) {
        if (apiTeam == null || apiTeam.getId() == null) return null;
        return teamRepository.findById(apiTeam.getId().longValue())
                .orElseGet(() -> {
                    Team t = new Team();
                    t.setId(apiTeam.getId().longValue());
                    t.setName(apiTeam.getName() != null ? apiTeam.getName() : "Unknown Team");
                    t.setLogoUrl(apiTeam.getLogo());
                    t.setLeague(league);
                    return teamRepository.save(t);
                });
    }

    private void createHistoricalSeasonalReport(Player player, int seasonYear, Statistic apiStat, double difficultyMultiplier) {
        if (seasonalReportRepository.existsByPlayerIdAndSeasonYear(player.getId(), seasonYear)) {
            return;
        }

        int minutes = apiStat.getGames() != null && apiStat.getGames().getMinutes() != null ? apiStat.getGames().getMinutes() : 0;
        int goals = apiStat.getGoals() != null && apiStat.getGoals().getTotal() != null ? apiStat.getGoals().getTotal() : 0;
        int assists = apiStat.getGoals() != null && apiStat.getGoals().getAssists() != null ? apiStat.getGoals().getAssists() : 0;

        double rating = apiStat.getGames() != null && apiStat.getGames().getRating() != null ? parseDoubleSafely(apiStat.getGames().getRating()) : 0.0;
        double weightedRating = rating * difficultyMultiplier;

        double goalsPer90 = minutes > 0 ? ((double) goals / minutes) * 90.0 : 0.0;
        double assistsPer90 = minutes > 0 ? ((double) assists / minutes) * 90.0 : 0.0;

        SeasonalReport report = SeasonalReport.builder()
                .player(player)
                .seasonYear(seasonYear)
                .minutesPlayed(minutes)
                .goalsPer90(goalsPer90)
                .assistsPer90(assistsPer90)
                .avgWeightedRating(weightedRating)
                .source(SeasonalReport.ReportSource.API_HISTORICAL)
                .avgScoutScore(0.0)
                .build();

        report = seasonalReportRepository.save(report);

        Map<String, Metric> metricMap = metricRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Metric::getName,
                        m -> m,
                        (existing, replacement) -> existing
                ));

        List<SeasonalValuedMetric> valuedMetrics = new ArrayList<>();

        if (apiStat.getShots() != null) {
            addMetric("Shots Total", apiStat.getShots().getTotal(), report, metricMap, valuedMetrics);
            addMetric("Shots On Target", apiStat.getShots().getOn(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getPasses() != null) {
            addMetric("Total Passes", apiStat.getPasses().getTotal(), report, metricMap, valuedMetrics);
            addMetric("Key Passes", apiStat.getPasses().getKey(), report, metricMap, valuedMetrics);
            addMetric("Pass Accuracy", apiStat.getPasses().getAccuracy(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getTackles() != null) {
            addMetric("Tackles", apiStat.getTackles().getTotal(), report, metricMap, valuedMetrics);
            addMetric("Blocks", apiStat.getTackles().getBlocks(), report, metricMap, valuedMetrics);
            addMetric("Interceptions", apiStat.getTackles().getInterceptions(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getDribbles() != null) {
            addMetric("Dribble Attempts", apiStat.getDribbles().getAttempts(), report, metricMap, valuedMetrics);
            addMetric("Successful Dribbles", apiStat.getDribbles().getSuccess(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getDuels() != null) {
            addMetric("Total Duels", apiStat.getDuels().getTotal(), report, metricMap, valuedMetrics);
            addMetric("Duels Won", apiStat.getDuels().getWon(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getFouls() != null) {
            addMetric("Fouls Drawn", apiStat.getFouls().getDrawn(), report, metricMap, valuedMetrics);
            addMetric("Fouls Committed", apiStat.getFouls().getCommitted(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getCards() != null) {
            addMetric("Yellow Cards", apiStat.getCards().getYellow(), report, metricMap, valuedMetrics);
            addMetric("Red Cards", apiStat.getCards().getRed(), report, metricMap, valuedMetrics);
        }
        if (apiStat.getPenalty() != null) {
            addMetric("Penalties Won", apiStat.getPenalty().getWon(), report, metricMap, valuedMetrics);
            addMetric("Penalties Scored", apiStat.getPenalty().getScored(), report, metricMap, valuedMetrics);
        }

        seasonalValuedMetricRepository.saveAll(valuedMetrics);
    }

    private void addMetric(String name, Object rawValue, SeasonalReport report, Map<String, Metric> metricMap, List<SeasonalValuedMetric> list) {
        Double value = parseDoubleSafely(rawValue);
        // Default to 0.0 if not tracked or null in API-Football
        double finalValue = value != null ? value : 0.0;
        if (metricMap.containsKey(name)) {
            list.add(SeasonalValuedMetric.builder()
                    .seasonalReport(report)
                    .metric(metricMap.get(name))
                    .aggregatedValue(finalValue)
                    .build());
        }
    }

    private Double parseDoubleSafely(Object obj) {
        if (obj == null) return null;
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}