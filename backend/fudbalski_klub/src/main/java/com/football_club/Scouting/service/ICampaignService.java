package com.football_club.Scouting.service;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.CampaignDetailsDTO;
import com.football_club.Scouting.dto.CampaignSaveDTO;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.model.Campaign;
import java.util.List;

public interface ICampaignService {
    Campaign createCampaign(CampaignSaveDTO campaign, User owner);
    Campaign getCampaignById(Long id);
    List<Campaign> getAllCampaigns();
    Campaign updateCampaign(Long id, Campaign campaign);
    void deleteCampaign(Long id);
    CampaignDetailsDTO getCampaignDetailsById(Long id);
    List<PlayerRecommendationDTO> getCampaignRecommendations(Long campaignId);
}