package com.football_club.Scouting.controller;

import com.football_club.Auth.service.IUserService;
import com.football_club.Scouting.dto.ScoutActivePlayerDTO;
import com.football_club.Scouting.dto.ScoutPerformanceDTO;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Scouting.service.impl.ScoutManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scout-management")
@RequiredArgsConstructor
public class ScoutManagementController {

    private final ScoutManagementService scoutManagementService;
    private final IUserService userService;

    @GetMapping("/performances")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ScoutPerformanceDTO>> getScoutPerformances() {
        return ResponseEntity.ok(scoutManagementService.getScoutPerformances());
    }

    @GetMapping("/scouts/{id}/active-players")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ScoutActivePlayerDTO>> getActivePlayersForScout(@PathVariable Long id) {
        return ResponseEntity.ok(scoutManagementService.getActivePlayersForScout(id));
    }

    @PatchMapping("/scouts/{id}/region")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<String> setScoutRegion(@PathVariable Long id, @RequestParam Region region) {
        userService.assignRegionToScout(id, region);
        return ResponseEntity.ok("Region uspešno dodeljen skautu.");
    }
}