package com.football_club.Scouting.controller;

import com.football_club.Scouting.dto.SeasonalReportDTO;
import com.football_club.Scouting.service.ISeasonalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seasonal-reports")
@RequiredArgsConstructor
public class SeasonalReportController {

    private final ISeasonalReportService seasonalReportService;

    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<SeasonalReportDTO>> getSeasonalReportsByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(seasonalReportService.getSeasonalReportsByPlayerId(playerId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<SeasonalReportDTO> getSeasonalReportById(@PathVariable Long id) {
        return ResponseEntity.ok(seasonalReportService.getSeasonalReportDTOById(id));
    }
}