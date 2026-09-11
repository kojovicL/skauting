package com.football_club.Scouting.service;

import com.football_club.Scouting.model.Campaign;
import java.util.List;

public interface ICampaignService {
    Campaign createCampaign(Campaign campaign);
    Campaign getCampaignById(Long id);
    List<Campaign> getAllCampaigns();
    Campaign updateCampaign(Long id, Campaign campaign);
    void deleteCampaign(Long id);
}