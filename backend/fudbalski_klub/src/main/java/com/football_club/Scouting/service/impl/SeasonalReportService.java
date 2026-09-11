package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.SeasonalReport;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import com.football_club.Scouting.service.ISeasonalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

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
}