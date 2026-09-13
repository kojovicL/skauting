package com.football_club.Scouting.controller;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.ReportDTO;
import com.football_club.Scouting.dto.ReportSaveDTO;
import com.football_club.Scouting.dto.ValuedMetricSaveDTO;
import com.football_club.Scouting.model.ApiMatchStat;
import com.football_club.Scouting.model.Metric;
import com.football_club.Scouting.repository.ApiMatchStatRepository;
import com.football_club.Scouting.repository.MetricRepository;
import com.football_club.Scouting.service.IReportService;
import com.football_club.Scouting.service.impl.MatchProcessingCronService;
import com.football_club.Scouting.service.impl.PlayerTransferSyncCronService;
import com.football_club.Scouting.service.impl.SeasonalReportCronService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class CronTestController {

    private final MatchProcessingCronService matchCronService;
    private final PlayerTransferSyncCronService transferCronService;
    private final SeasonalReportCronService seasonalCronService;
    private final IReportService reportService;
    private final ApiMatchStatRepository apiMatchStatRepository;
    private final MetricRepository metricRepository;

    @PostMapping("/trigger-match-cron")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> triggerMatchCron() {
        matchCronService.processDailyMatches();
        return ResponseEntity.ok(Map.of("message", "Daily match processing executed successfully."));
    }

    @PostMapping("/trigger-transfer-cron")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> triggerTransferCron() {
        transferCronService.syncAllPlayerTransfers();
        return ResponseEntity.ok(Map.of("message", "Daily transfer sync executed successfully."));
    }

    @PostMapping("/trigger-seasonal-cron")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> triggerSeasonalCron(@RequestParam(required = false) Long seasonId) {
        if (seasonId != null) {
            seasonalCronService.forceGenerateForSeason(seasonId);
            return ResponseEntity.ok(Map.of("message", "Seasonal reports force-generated for season ID: " + seasonId));
        }
        seasonalCronService.generateEndOfSeasonReports();
        return ResponseEntity.ok(Map.of("message", "Seasonal report cron executed for today's ended seasons."));
    }

    @PostMapping("/auto-create-report")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<ReportDTO> autoCreateReport(
            @RequestParam Long playerId,
            @RequestParam Long matchId,
            @RequestParam(defaultValue = "Odličan meč. Igrač pokazuje visok nivo discipline i taktičke zrelosti.") String commentary,
            @AuthenticationPrincipal User userDetails) {

        ApiMatchStat stat = apiMatchStatRepository.findByPlayerIdAndMatchId(playerId, matchId)
                .orElseThrow(() -> new NoSuchElementException("ApiMatchStat not found for player " + playerId + " and match " + matchId));

        // Auto-fill metrics from ApiMatchStat, falling back to 7.0 for any unrecorded system metric
        List<ValuedMetricSaveDTO> metrics = new ArrayList<>();
        if (stat.getMatchMetrics() != null && !stat.getMatchMetrics().isEmpty()) {
            stat.getMatchMetrics().forEach(m -> metrics.add(
                    ValuedMetricSaveDTO.builder()
                            .metricId(m.getMetric().getId())
                            .value(m.getValue())
                            .build()
            ));
        } else {
            for (Metric m : metricRepository.findAll()) {
                metrics.add(ValuedMetricSaveDTO.builder()
                        .metricId(m.getId())
                        .value(7.5)
                        .build());
            }
        }

        ReportSaveDTO dto = ReportSaveDTO.builder()
                .playerId(playerId)
                .matchId(matchId)
                .overallCommentary(commentary)
                .minutesPlayed(stat.getMinutesPlayed())
                .shirtNumber(stat.getShirtNumber())
                .isSubstitute(stat.getIsSubstitute())
                .isCaptain(stat.getIsCaptain())
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .rawRating(stat.getRawRating())
                .metrics(metrics)
                .build();

        return ResponseEntity.ok(reportService.createReport(dto, userDetails.getId()));
    }
}