package com.football_club.Scouting.controller;

import com.football_club.Clients.APIFootballClient;
import com.football_club.Scouting.dto.OnboardPlayerRequest;
import com.football_club.Scouting.service.IPlayerOnboardingService;
import com.football_club.Scouting.service.impl.PlayerOnboardingService;
import com.football_club.dto.apifootball.Player;
import com.football_club.dto.apifootball.PlayerProfileResponse;
import com.football_club.dto.apifootball.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onboarding")
@RequiredArgsConstructor
public class PlayerOnboardingController {

    private final APIFootballClient apiClient;
    private final IPlayerOnboardingService onboardingService;

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<Player>> searchPlayers(@RequestParam String name) {
        PlayerProfileResponse apiResponse = apiClient.searchPlayers(name);
        List<Response> response = apiResponse.getResponse();
        if (response.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Player> foundPlayers = response.stream()
                .map(Response::getPlayer)
                .toList();
        return ResponseEntity.ok(foundPlayers);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> onboardPlayerToCampaign(@RequestBody OnboardPlayerRequest request) {
        onboardingService.onboardPlayer(request);
        return ResponseEntity.ok().build();
    }
}