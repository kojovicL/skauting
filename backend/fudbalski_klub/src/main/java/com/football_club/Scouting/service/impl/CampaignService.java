package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Campaign;
import com.football_club.Scouting.repository.CampaignRepository;
import com.football_club.Scouting.service.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public Campaign createCampaign(Campaign campaign) {
        return campaignRepository.save(campaign);
    }

    @Override
    @Transactional(readOnly = true)
    public Campaign getCampaignById(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kampanja sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Campaign> getAllCampaigns() {
        return campaignRepository.findAll();
    }

    @Override
    @Transactional
    public Campaign updateCampaign(Long id, Campaign campaignDetails) {
        Campaign campaign = getCampaignById(id);
        campaign.setName(campaignDetails.getName());
        campaign.setDescription(campaignDetails.getDescription());
        campaign.setTargetPosition(campaignDetails.getTargetPosition());
        campaign.setStatus(campaignDetails.getStatus());
        campaign.setStartDate(campaignDetails.getStartDate());
        campaign.setEndDate(campaignDetails.getEndDate());
        return campaignRepository.save(campaign);
    }

    @Override
    @Transactional
    public void deleteCampaign(Long id) {
        if (!campaignRepository.existsById(id)) {
            throw new NoSuchElementException("Kampanja sa ID-em " + id + " ne postoji.");
        }
        campaignRepository.deleteById(id);
    }
}