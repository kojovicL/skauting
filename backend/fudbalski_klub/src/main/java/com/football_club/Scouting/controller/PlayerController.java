package com.football_club.Scouting.controller;

import com.football_club.Scouting.dto.PlayerSearchDTO;
import com.football_club.Scouting.service.IPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final IPlayerService playerService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<PlayerSearchDTO>> searchLocalPlayers(@RequestParam String query) {
        List<PlayerSearchDTO> results = playerService.searchPlayers(query).stream().map(p ->
                PlayerSearchDTO.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .surname(p.getSurname())
                        .age(p.getAge())
                        .nationality(p.getNationality())
                        .photoUrl(p.getPhotoUrl())
                        .position(p.getPosition() != null ? p.getPosition().name() : null)
                        .build()
        ).collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<com.football_club.Scouting.dto.PlayerDetailsDTO> getPlayerDetails(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayerDetails(id));
    }

    @GetMapping("/{id}/recent-matches")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<com.football_club.Scouting.dto.RecentMatchDTO>> getRecentMatches(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getRecentMatches(id));
    }
}