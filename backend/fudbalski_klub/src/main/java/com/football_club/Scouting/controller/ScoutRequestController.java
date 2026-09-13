package com.football_club.Scouting.controller;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.ScoutRequestDTO;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Scouting.service.IScoutRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scout-requests")
@RequiredArgsConstructor
public class ScoutRequestController {

    private final IScoutRequestService scoutRequestService;

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ScoutRequestDTO>> getPendingRequests(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(scoutRequestService.getPendingRequestsForUser(userDetails));
    }

    @GetMapping("/region/{region}")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ScoutRequestDTO>> getRequestsByRegion(@PathVariable Region region) {
        return ResponseEntity.ok(scoutRequestService.getRequestsByRegion(region));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ScoutRequestDTO>> getAllRequests() {
        return ResponseEntity.ok(scoutRequestService.getAllRequests());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<ScoutRequestDTO> getRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(scoutRequestService.getRequestById(id));
    }

    @PostMapping("/{id}/claim")
    @PreAuthorize("hasAnyRole('SCOUT', 'ADMIN')")
    public ResponseEntity<ScoutRequestDTO> claimRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal User userDetails) {
        ScoutRequestDTO claimed = scoutRequestService.claimRequest(id, userDetails.getId());
        return ResponseEntity.ok(claimed);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        scoutRequestService.deleteRequest(id);
        return ResponseEntity.noContent().build();
    }
}