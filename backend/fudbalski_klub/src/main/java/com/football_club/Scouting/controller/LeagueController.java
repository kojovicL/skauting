package com.football_club.Scouting.controller;

import com.football_club.Scouting.dto.LeagueDTO;
import com.football_club.Scouting.dto.LeagueSaveDTO;
import com.football_club.Scouting.service.ILeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final ILeagueService leagueService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDTO> createLeague(@RequestBody LeagueSaveDTO leagueSaveDTO) {
        LeagueDTO createdLeague = leagueService.createLeague(leagueSaveDTO);
        return new ResponseEntity<>(createdLeague, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('HEAD_COACH', 'ASSISTANT_COACH', 'SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<LeagueDTO> getLeagueById(@PathVariable Long id) {
        LeagueDTO leagueDTO = leagueService.getLeagueById(id);
        return ResponseEntity.ok(leagueDTO);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('HEAD_COACH', 'ASSISTANT_COACH', 'SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<LeagueDTO>> getAllLeagues() {
        List<LeagueDTO> leagues = leagueService.getAllLeagues();
        return ResponseEntity.ok(leagues);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LeagueDTO> updateLeague(@PathVariable Long id, @RequestBody LeagueSaveDTO leagueSaveDTO) {
        LeagueDTO updatedLeague = leagueService.updateLeague(id, leagueSaveDTO);
        return ResponseEntity.ok(updatedLeague);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLeague(@PathVariable Long id) {
        leagueService.deleteLeague(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{leagueId}/clubs/{clubId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addClubToLeague(@PathVariable Long leagueId, @PathVariable Long teamId) {
        leagueService.addTeamToLeague(leagueId, teamId);
        return ResponseEntity.ok().build();
    }
}
