package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.model.SeasonalReport;
import com.football_club.Scouting.model.SeasonalValuedMetric;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import com.football_club.Scouting.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService implements IRecommendationService {

    private final SeasonalReportRepository seasonalReportRepository;

    public List<PlayerRecommendationDTO> getRecommendations(Position position, Map<Long, Double> metricWeights) {
        if (metricWeights == null || metricWeights.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Fetch latest seasonal reports for all players in this position
        List<SeasonalReport> latestReports = seasonalReportRepository.findLatestByPositionWithMetrics(position);

        // Handle edge case: A player might have multiple reports for their max season year (e.g. played in 2 leagues)
        // We select the report with the highest minutes played as their primary indicator for the year.
        Map<Long, SeasonalReport> primaryReportsMap = new HashMap<>();
        for (SeasonalReport report : latestReports) {
            Long pId = report.getPlayer().getId();
            if (!primaryReportsMap.containsKey(pId) || report.getMinutesPlayed() > primaryReportsMap.get(pId).getMinutesPlayed()) {
                primaryReportsMap.put(pId, report);
            }
        }

        List<PlayerRecommendationDTO> recommendations = new ArrayList<>();

        // 2. Calculate Weighted Percentile Scores
        for (SeasonalReport report : primaryReportsMap.values()) {
            double rawScoreSum = 0.0;

            for (SeasonalValuedMetric vm : report.getCustomMetrics()) {
                if (metricWeights.containsKey(vm.getMetric().getId())) {
                    double weight = metricWeights.get(vm.getMetric().getId());
                    double percentile = vm.getPercentile() != null ? vm.getPercentile() : 0.0;

                    rawScoreSum += percentile * weight;
                }
            }

            // Apply the difficulty multiplier of the league they achieved these percentiles in
            double finalScore = rawScoreSum * report.getLeague().getDifficultyMultiplier();

            recommendations.add(new PlayerRecommendationDTO(
                    report.getPlayer().getId(),
                    report.getPlayer().getName(),
                    report.getPlayer().getSurname(),
                    report.getPlayer().getPhotoUrl(),
                    finalScore,
                    "SEASONAL_PERCENTILE"
            ));
        }

        // 3. Sort descending (highest score first) and return top 5
        return recommendations.stream()
                .sorted(Comparator.comparingDouble(PlayerRecommendationDTO::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}