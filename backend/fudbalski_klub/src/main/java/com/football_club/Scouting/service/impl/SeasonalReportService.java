package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.dto.SeasonalReportDTO;
import com.football_club.Scouting.dto.SeasonalValuedMetricDTO;
import com.football_club.Scouting.model.SeasonalReport;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import com.football_club.Scouting.service.ISeasonalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeasonalReportService implements ISeasonalReportService {

    private final SeasonalReportRepository seasonalReportRepository;

    @Override
    @Transactional
    public SeasonalReport createSeasonalReport(SeasonalReport report) {
        return seasonalReportRepository.save(report);
    }

    @Override
    @Transactional(readOnly = true)
    public SeasonalReport getSeasonalReportById(Long id) {
        return seasonalReportRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sezonski izveštaj sa ID-em " + id + " nije pronađen."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalReport> getAllSeasonalReports() {
        return seasonalReportRepository.findAll();
    }

    @Override
    @Transactional
    public SeasonalReport updateSeasonalReport(Long id, SeasonalReport reportDetails) {
        SeasonalReport report = getSeasonalReportById(id);
        report.setAvgScoutScore(reportDetails.getAvgScoutScore());
        return seasonalReportRepository.save(report);
    }

    @Override
    @Transactional
    public void deleteSeasonalReport(Long id) {
        if (!seasonalReportRepository.existsById(id)) {
            throw new NoSuchElementException("Sezonski izveštaj sa ID-em " + id + " ne postoji.");
        }
        seasonalReportRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeasonalReportDTO> getSeasonalReportsByPlayerId(Long playerId) {
        List<SeasonalReport> reports = seasonalReportRepository.findByPlayerIdWithMetrics(playerId);
        return reports.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private SeasonalReportDTO mapToDTO(SeasonalReport report) {
        List<SeasonalValuedMetricDTO> metricDTOs = report.getCustomMetrics() != null
                ? report.getCustomMetrics().stream()
                .map(cm -> SeasonalValuedMetricDTO.builder()
                        .id(cm.getId())
                        .metricId(cm.getMetric().getId())
                        .metricName(cm.getMetric().getName())
                        .category(cm.getMetric().getCategory())
                        .type(cm.getMetric().getType())
                        .aggregatedValue(cm.getAggregatedValue())
                        .build())
                .collect(Collectors.toList())
                : Collections.emptyList();

        return SeasonalReportDTO.builder()
                .id(report.getId())
                .playerId(report.getPlayer().getId())
                .playerName(report.getPlayer().getName())
                .playerSurname(report.getPlayer().getSurname())
                .seasonYear(report.getSeasonYear())
                .source(report.getSource())
                .avgScoutScore(report.getAvgScoutScore())
                .metrics(metricDTOs)
                .build();
    }
}