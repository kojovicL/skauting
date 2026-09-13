package com.football_club.Scouting.controller;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.CampaignDetailsDTO;
import com.football_club.Scouting.dto.CampaignSaveDTO;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.model.Campaign;
import com.football_club.Scouting.service.impl.CampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> createCampaign(
            @RequestBody CampaignSaveDTO campaign,
            @AuthenticationPrincipal User userDetails) {
        Campaign created = campaignService.createCampaign(campaign, userDetails);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<CampaignDetailsDTO> getCampaignDetails(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignDetailsById(id));
    }

    @GetMapping("/{id}/recommendations")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<PlayerRecommendationDTO>> getCampaignRecommendations(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignRecommendations(id));
    }
}