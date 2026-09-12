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

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;

    @Override
    @Transactional
    public Campaign createCampaign(CampaignSaveDTO campaign, User owner) {
        Campaign campaignEntity = new Campaign();
        campaignEntity.setName(campaign.getName());
        campaignEntity.setDescription(campaign.getDescription());
        campaignEntity.setTargetPosition(campaign.getTargetPosition());
        campaignEntity.setEndDate(campaign.getEndDate());
        if (campaign.getStartDate() != null && !campaign.getStartDate().isEqual(LocalDate.now())) {
            campaignEntity.setStartDate(campaign.getStartDate());
            campaignEntity.setStatus(CampaignStatus.PENDING);
        }
        campaignEntity.setDirector(owner);
        return campaignRepository.save(campaignEntity);
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
                .monitoredPlayers(players)
                .build();
    }
}