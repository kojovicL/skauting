package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.config.RabbitMQConfig;
import com.football_club.Scouting.dto.SeasonalSummaryRequestDTO;
import com.football_club.Scouting.model.*;
import com.football_club.Scouting.repository.ReportRepository;
import com.football_club.Scouting.repository.SeasonRepository;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import com.football_club.Scouting.service.DateTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonalReportCronService {

    private final SeasonRepository seasonRepository;
    private final ReportRepository reportRepository;
    private final SeasonalReportRepository seasonalReportRepository;
    private final DateTimeService dateTimeService;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0 3 * * *") // Runs daily at 3:00 AM
    @Transactional
    public void generateEndOfSeasonReports() {
        LocalDate today = dateTimeService.getCurrentDate();
        List<Season> endedSeasons = seasonRepository.findByEndDate(today);

        for (Season season : endedSeasons) {
            log.info("Generating seasonal reports for Season ID: {}", season.getId());
            processSeasonReports(season);
        }
    }

    @Transactional
    public void forceGenerateForSeason(Long seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new NoSuchElementException("Season not found with ID: " + seasonId));
        log.info("Force generating seasonal reports for Season ID: {}", season.getId());
        processSeasonReports(season);
    }

    private void processSeasonReports(Season season) {
        List<Report> seasonReports = reportRepository.findReportsBySeasonId(season.getId());

        // Group reports by Player
        Map<Player, List<Report>> reportsByPlayer = seasonReports.stream()
                .collect(Collectors.groupingBy(Report::getPlayer));

        for (Map.Entry<Player, List<Report>> entry : reportsByPlayer.entrySet()) {
            Player player = entry.getKey();
            List<Report> playerReports = entry.getValue();

            // Check if a report already exists for this player, season, and league
            Optional<SeasonalReport> existingReportOpt = seasonalReportRepository
                    .findByPlayerIdAndSeasonYearAndLeagueId(player.getId(), season.getYear(), season.getLeague().getId());

            // If an internal report was already generated, do not re-run
            if (existingReportOpt.isPresent() && existingReportOpt.get().getSource() == SeasonalReport.ReportSource.INTERNAL_CALCULATED) {
                continue;
            }

            int totalMinutes = 0;
            int totalGoals = 0;
            int totalAssists = 0;
            double totalWeightedRating = 0;
            List<String> commentaries = new ArrayList<>();
            Map<Metric, List<Double>> metricValuesMap = new HashMap<>();

            for (Report r : playerReports) {
                totalMinutes += r.getMinutesPlayed() != null ? r.getMinutesPlayed() : 0;
                totalGoals += r.getGoals() != null ? r.getGoals() : 0;
                totalAssists += r.getAssists() != null ? r.getAssists() : 0;
                totalWeightedRating += r.getWeightedRating() != null ? r.getWeightedRating() : 0.0;

                if (r.getOverallCommentary() != null && !r.getOverallCommentary().isBlank()) {
                    commentaries.add(r.getOverallCommentary());
                }

                if (r.getValuedMetrics() != null) {
                    for (ValuedMetric vm : r.getValuedMetrics()) {
                        metricValuesMap.computeIfAbsent(vm.getMetric(), k -> new ArrayList<>()).add(vm.getValue());
                    }
                }
            }

            double goalsPer90 = totalMinutes > 0 ? ((double) totalGoals / totalMinutes) * 90.0 : 0.0;
            double assistsPer90 = totalMinutes > 0 ? ((double) totalAssists / totalMinutes) * 90.0 : 0.0;
            double avgWeightedRating = playerReports.isEmpty() ? 0.0 : totalWeightedRating / playerReports.size();

            SeasonalReport seasonalReport;

            if (existingReportOpt.isPresent()) {
                // 1. Upgrade the existing API_HISTORICAL report
                seasonalReport = existingReportOpt.get();
                seasonalReport.setMinutesPlayed(totalMinutes);
                seasonalReport.setGoalsPer90(goalsPer90);
                seasonalReport.setAssistsPer90(assistsPer90);
                seasonalReport.setAvgWeightedRating(avgWeightedRating);
                seasonalReport.setSource(SeasonalReport.ReportSource.INTERNAL_CALCULATED);
                seasonalReport.setSeasonSummary("Generisanje izveštaja u toku...");

                // Clear old historical API metrics so they are replaced by club scout metrics
                if (seasonalReport.getCustomMetrics() != null) {
                    seasonalReport.getCustomMetrics().clear();
                } else {
                    seasonalReport.setCustomMetrics(new ArrayList<>());
                }
            } else {
                // 2. Create a fresh report if none existed
                seasonalReport = SeasonalReport.builder()
                        .player(player)
                        .league(season.getLeague())
                        .seasonYear(season.getYear())
                        .minutesPlayed(totalMinutes)
                        .goalsPer90(goalsPer90)
                        .assistsPer90(assistsPer90)
                        .avgWeightedRating(avgWeightedRating)
                        .source(SeasonalReport.ReportSource.INTERNAL_CALCULATED)
                        .seasonSummary("Generisanje izveštaja u toku...")
                        .customMetrics(new ArrayList<>())
                        .build();
            }

            // Map the aggregated valued metrics
            for (Map.Entry<Metric, List<Double>> metricEntry : metricValuesMap.entrySet()) {
                double avgValue = metricEntry.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                seasonalReport.getCustomMetrics().add(SeasonalValuedMetric.builder()
                        .seasonalReport(seasonalReport)
                        .metric(metricEntry.getKey())
                        .aggregatedValue(avgValue)
                        .build());
            }

            SeasonalReport savedReport = seasonalReportRepository.save(seasonalReport);

            // Dispatch async LLM request to RabbitMQ
            if (!commentaries.isEmpty()) {
                SeasonalSummaryRequestDTO requestDTO = SeasonalSummaryRequestDTO.builder()
                        .seasonalReportId(savedReport.getId())
                        .playerName(player.getName() + " " + player.getSurname())
                        .leagueName(season.getLeague().getName())
                        .seasonYear(season.getYear())
                        .matchCommentaries(commentaries)
                        .build();

                rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_REQUEST, requestDTO);
            } else {
                savedReport.setSeasonSummary("Nedovoljno tekstualnih podataka za generisanje sezonskog izveštaja.");
                seasonalReportRepository.save(savedReport);
            }
        }
    }
}