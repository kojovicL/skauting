package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.CampaignDetailsDTO;
import com.football_club.Scouting.dto.CampaignSaveDTO;
import com.football_club.Scouting.model.Campaign;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.enums.CampaignStatus;
import com.football_club.Scouting.repository.CampaignRepository;
import com.football_club.Scouting.service.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Scouting.service.IPlayerOnboardingService;
import com.football_club.Scouting.dto.OnboardPlayerRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final IPlayerOnboardingService playerOnboardingService;

    @Override
    @Transactional
    public Campaign createCampaign(CampaignSaveDTO campaignDto, User owner) {
        Campaign campaignEntity = new Campaign();
        campaignEntity.setName(campaignDto.getName());
        campaignEntity.setDescription(campaignDto.getDescription());
        campaignEntity.setTargetPosition(campaignDto.getTargetPosition());
        campaignEntity.setEndDate(campaignDto.getEndDate());
        campaignEntity.setRegion(campaignDto.getRegion() != null ? campaignDto.getRegion() : Region.GLOBAL);

        if (campaignDto.getStartDate() != null && !campaignDto.getStartDate().isEqual(LocalDate.now())) {
            campaignEntity.setStartDate(campaignDto.getStartDate());
            campaignEntity.setStatus(CampaignStatus.PENDING);
        }
        campaignEntity.setDirector(owner);
        Campaign savedCampaign = campaignRepository.save(campaignEntity);

        // Auto-onboard candidates explicitly requested by Director
        if (campaignDto.getCandidateApiIds() != null && !campaignDto.getCandidateApiIds().isEmpty()) {
            for (Long apiId : campaignDto.getCandidateApiIds()) {
                try {
                    OnboardPlayerRequest req = new OnboardPlayerRequest(apiId, savedCampaign.getId(), null);
                    playerOnboardingService.onboardPlayer(req, owner);
                } catch (Exception e) {
                    throw new RuntimeException("Neuspešno dodavanje kandidata " + apiId + ": " + e.getMessage(), e);
                }
            }
        }
        return savedCampaign;
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

    @Transactional(readOnly = true)
    public CampaignDetailsDTO getCampaignDetailsById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kampanja nije pronađena."));

        List<CampaignDetailsDTO.MonitoredPlayerBasicDTO> players = campaign.getMonitoredPlayers().stream()
                .map(mp -> {
                    Player p = mp.getPlayer();
                    return CampaignDetailsDTO.MonitoredPlayerBasicDTO.builder()
                            .playerId(p.getId())
                            .name(p.getName())
                            .surname(p.getSurname())
                            .photoUrl(p.getPhotoUrl())
                            .currentTeamName(p.getCurrentTeam() != null ? p.getCurrentTeam().getName() : "Slobodan igrač")
                            .age(p.getAge())
                            .build();
                }).toList();

        return CampaignDetailsDTO.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .description(campaign.getDescription())
                .targetPosition(campaign.getTargetPosition())
                .status(campaign.getStatus())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .directorId(campaign.getDirector().getId())
                .region(campaign.getRegion())
                .monitoredPlayers(players)
                .build();
    }
}