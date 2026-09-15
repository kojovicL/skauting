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

    @GetMapping("/my/active")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<CampaignDetailsDTO>> getMyActiveCampaigns(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(campaignService.getMyActiveCampaignsDetails(userDetails.getId()));
    }

    @GetMapping("/my/completed")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<CampaignDetailsDTO>> getMyCompletedCampaigns(@AuthenticationPrincipal User userDetails) {
        return ResponseEntity.ok(campaignService.getMyCompletedCampaignsDetails(userDetails.getId()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> updateCampaign(@PathVariable Long id, @RequestBody CampaignSaveDTO dto) {
        Campaign campaign = campaignService.getCampaignById(id);
        campaign.setName(dto.getName());
        campaign.setDescription(dto.getDescription());
        campaign.setTargetPosition(dto.getTargetPosition());
        campaign.setStartDate(dto.getStartDate());
        campaign.setEndDate(dto.getEndDate());
        if (dto.getRegion() != null) {
            campaign.setRegion(dto.getRegion());
        }
        campaignService.updateCampaign(id, campaign);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        campaignService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/end")
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Void> endCampaign(@PathVariable Long id) {
        campaignService.endCampaign(id);
        return ResponseEntity.ok().build();
    }
}