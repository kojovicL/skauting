package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.model.MetricContext;
import com.football_club.Scouting.model.Report;
import com.football_club.Scouting.model.ValuedMetric;
import com.football_club.Scouting.repository.GameMetricRepository;
import com.football_club.Scouting.repository.MetricContextRepository;
import com.football_club.Scouting.repository.ReportRepository;
import com.football_club.Scouting.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService implements IRecommendationService {
    private final MetricContextRepository contextRepository;
    private final ReportRepository reportRepository;
    private final GameMetricRepository gameMetricRepository;

    public List<PlayerRecommendationDTO> getRecommendations(Position position, Map<Long, Double> metricWeights) {
        if (metricWeights.isEmpty()) { return Collections.emptyList(); }

        Set<Long> selectedMetricIds = metricWeights.keySet();
        List<PlayerRecommendationDTO> recommendations = new ArrayList<>();

        // 1. dobavljanje konteksta metrika za normalizaciju
        Map<Long, MetricContext> contextMap = contextRepository
                .findByPositionAndMetricIdIn(position, selectedMetricIds)
                .stream()
                .collect(Collectors.toMap(MetricContext::getMetricId, metricContext -> metricContext));

        // 2. dobavi najnovije izvestaje i izracunaj score za igrace koji ih imaju
        List<Report> latestReports = reportRepository.findLatestReportsWithMetrics(position, selectedMetricIds);

        for (Report report : latestReports) {
            double rawScoreSum = 0.0;

            for (ValuedMetric vm : report.getValuedMetrics()) {
                MetricContext context = contextMap.get(vm.getMetric().getId());
                if (context != null) {
                    double normalizedValue = context.normalize(vm.getValue());
                    double weight = metricWeights.get(vm.getMetric().getId());
                    rawScoreSum += normalizedValue * weight;
                }
            }

            double finalScore = rawScoreSum * report.getLeagueMultiplierAtTime();
            recommendations.add(new PlayerRecommendationDTO(
                    report.getPlayer().getId(),
                    report.getPlayer().getName(),
                    report.getPlayer().getSurname(),
                    finalScore,
                    "LATEST_REPORT"
            ));
        }

        // 3. procesiraj sve igrace koji nemaju izvestaj (koristimo avg vrednost metrika u poslednjih 5 meceva odigranih)
        List<GameMetricRepository.FallbackMetricProjection> fallbackData = gameMetricRepository
                .findFallbackAverages(position.name(), selectedMetricIds);

        // grupisanje fallback vrednosti po igracima
        Map<Long, List<GameMetricRepository.FallbackMetricProjection>> groupedFallback = fallbackData
                        .stream()
                        .collect(Collectors.groupingBy(GameMetricRepository.FallbackMetricProjection::getPlayerId));

        groupedFallback.forEach((playerId, metrics) -> {
            GameMetricRepository.FallbackMetricProjection fallback = metrics.get(0);
            double rawScoreSum = 0.0;

            for (GameMetricRepository.FallbackMetricProjection m : metrics) {
                MetricContext context = contextMap.get(m.getMetricId());
                if (context != null) {
                    double normalizedAvg = context.normalize(m.getAvgValue());
                    double weight = metricWeights.get(m.getMetricId());
                    rawScoreSum += normalizedAvg * weight;
                }
            }

            double finalScore = rawScoreSum * 0.8; // penalty jer nemaju profesinalni izvestaj
            recommendations.add(new PlayerRecommendationDTO(
                    playerId,
                    fallback.getName(),
                    fallback.getSurname(),
                    finalScore,
                    "FALLBACK"
            ));
        });

        double maxScore = recommendations.stream()
                .mapToDouble(PlayerRecommendationDTO::getScore)
                .max()
                .orElse(0.0);

        if (maxScore > 0) {
            recommendations.forEach(r -> {
                double normalized = normalizeScore(maxScore, r.getScore());
                r.setScore(normalized);
            });
        } else if (!recommendations.isEmpty()) {
            recommendations.forEach(r -> r.setScore(100.0));
        }

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(PlayerRecommendationDTO::getScore).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    private double normalizeScore(double maxScore, double score) {
        return score / maxScore * 100.00;
    }
}
