package com.football_club.Scouting.controller;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.*;
import com.football_club.Scouting.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @GetMapping("/draft-data")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<ReportDraftDataDTO> getReportDraftData(
            @RequestParam Long playerId,
            @RequestParam Long matchId) {
        return ResponseEntity.ok(reportService.getReportDraftData(playerId, matchId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<ReportDTO> createReport(
            @RequestBody ReportSaveDTO reportSaveDTO,
            @AuthenticationPrincipal User userDetails) {
        ReportDTO createdReport = reportService.createReport(reportSaveDTO, userDetails.getId());
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<ReportDTO> getReportById(@PathVariable Long id) {
        ReportDTO reportDTO = reportService.getReportById(id);
        return ResponseEntity.ok(reportDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ReportDTO>> getAllReports() {
        List<ReportDTO> reports = reportService.getAllReports();
        return ResponseEntity.ok(reports);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<?> updateReport(
            @PathVariable Long id,
            @RequestBody ReportSaveDTO reportSaveDTO,
            @AuthenticationPrincipal User userDetails) {
        if (!userDetails.getRole().name().equals("ROLE_ADMIN")) {
            ReportDTO existing = reportService.getReportById(id);
            if (!existing.getScoutId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Nemate dozvolu da menjate tuđe izveštaje!");
            }
        }
        ReportDTO updatedReport = reportService.updateReport(id, reportSaveDTO, userDetails.getId());
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<?> deleteReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User userDetails) {
        if (!userDetails.getRole().name().equals("ROLE_ADMIN")) {
            ReportDTO existing = reportService.getReportById(id);
            if (!existing.getScoutId().equals(userDetails.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Nemate dozvolu da obrišete tuđe izveštaje!");
            }
        }
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<List<ReportDTO>> getMyReports(@AuthenticationPrincipal User userDetails) {
        List<ReportDTO> reports = reportService.getReportsByScout(userDetails.getId());
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/scout/{scoutId}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ReportDTO>> getReportsByScout(@PathVariable Long scoutId) {
        List<ReportDTO> reports = reportService.getReportsByScout(scoutId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ReportDTO>> getReportsByPlayer(@PathVariable Long playerId) {
        List<ReportDTO> reports = reportService.getReportsByPlayer(playerId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/player/{playerId}/latest")
    public ResponseEntity<ReportDTO> getLatestReportForPlayer(@PathVariable Long playerId) {
        ReportDTO report = reportService.getLatestReportByPlayer(playerId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/pending-matches")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<List<PendingReportMatchDTO>> getPendingMatches(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(reportService.getPendingMatchesForScout(userDetails.getId()));
    }

    @GetMapping("/upcoming-tasks")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<List<UpcomingMatchTaskDTO>> getUpcomingTasks(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(reportService.getUpcomingMatchTasksForScout(userDetails.getId()));
    }
}