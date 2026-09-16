package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.SeasonalReportDTO;
import com.football_club.Scouting.model.SeasonalReport;
import java.util.List;

public interface ISeasonalReportService {
    SeasonalReport createSeasonalReport(SeasonalReport report);
    SeasonalReport getSeasonalReportById(Long id);
    List<SeasonalReport> getAllSeasonalReports();
    SeasonalReport updateSeasonalReport(Long id, SeasonalReport report);
    void deleteSeasonalReport(Long id);
    List<SeasonalReportDTO> getSeasonalReportsByPlayerId(Long playerId);
    SeasonalReportDTO getSeasonalReportDTOById(Long id);
}