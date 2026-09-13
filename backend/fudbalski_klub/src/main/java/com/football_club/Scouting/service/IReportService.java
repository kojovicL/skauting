package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.PendingReportMatchDTO;
import com.football_club.Scouting.dto.ReportDTO;
import com.football_club.Scouting.dto.ReportDraftDataDTO;
import com.football_club.Scouting.dto.ReportSaveDTO;

import java.util.List;

public interface IReportService {
    ReportDraftDataDTO getReportDraftData(Long playerId, Long matchId);
    ReportDTO createReport(ReportSaveDTO dto, Long scoutId);
    ReportDTO getReportById(Long id);
    List<ReportDTO> getAllReports();
    ReportDTO updateReport(Long id, ReportSaveDTO dto, Long scoutId);
    void deleteReport(Long id);
    List<ReportDTO> getReportsByScout(Long scoutId);
    List<ReportDTO> getReportsByPlayer(Long playerId);
    ReportDTO getLatestReportByPlayer(Long playerId);
    List<PendingReportMatchDTO> getPendingMatchesForScout(Long scoutId);
}