package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.CampaignDetailsDTO;
import com.football_club.Scouting.dto.CampaignSaveDTO;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.model.Campaign;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.SeasonalReport;
import com.football_club.Scouting.model.enums.CampaignStatus;
import com.football_club.Scouting.repository.CampaignRepository;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import com.football_club.Scouting.service.ICampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Scouting.service.IPlayerOnboardingService;
import com.football_club.Scouting.dto.OnboardPlayerRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CampaignService implements ICampaignService {

    private final CampaignRepository campaignRepository;
    private final IPlayerOnboardingService playerOnboardingService;
    private final SeasonalReportRepository seasonalReportRepository;

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

    @Override
    @Transactional(readOnly = true)
    public List<CampaignDetailsDTO> getMyActiveCampaignsDetails(Long directorId) {
        // Fetch campaigns by director and filter only the active ones
        return campaignRepository.findByDirectorId(directorId).stream()
                .filter(c -> c.getStatus() == CampaignStatus.ACTIVE)
                .map(this::mapToCampaignDetailsDTO)
                .collect(Collectors.toList());
    }

    // Refactor your existing getCampaignDetailsById to use this helper
    @Transactional(readOnly = true)
    public CampaignDetailsDTO getCampaignDetailsById(Long id) {
        Campaign campaign = campaignRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Kampanja nije pronađena."));
        return mapToCampaignDetailsDTO(campaign);
    }

    // New private helper method extracted from your original code
    private CampaignDetailsDTO mapToCampaignDetailsDTO(Campaign campaign) {
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
                .targetPosition(campaign.getTargetPosition().getDisplayName())
                .status(campaign.getStatus())
                .startDate(campaign.getStartDate())
                .endDate(campaign.getEndDate())
                .directorId(campaign.getDirector().getId())
                .region(campaign.getRegion())
                .monitoredPlayers(players)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerRecommendationDTO> getCampaignRecommendations(Long campaignId) {
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new NoSuchElementException("Kampanja sa ID-em " + campaignId + " nije pronađena."));

        List<Long> playerIds = campaign.getMonitoredPlayers().stream()
                .map(mp -> mp.getPlayer().getId())
                .collect(Collectors.toList());

        if (playerIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<SeasonalReport> latestReports = seasonalReportRepository.findLatestForPlayersWithMetrics(playerIds);

        // Handle edge cases where a player has multiple reports for the same max year
        Map<Long, SeasonalReport> primaryReportsMap = new HashMap<>();
        for (SeasonalReport report : latestReports) {
            Long pId = report.getPlayer().getId();
            if (!primaryReportsMap.containsKey(pId) || report.getMinutesPlayed() > primaryReportsMap.get(pId).getMinutesPlayed()) {
                primaryReportsMap.put(pId, report);
            }
        }

        List<PlayerRecommendationDTO> recommendations = new ArrayList<>();

        for (SeasonalReport report : primaryReportsMap.values()) {
            double totalPercentile = 0.0;
            int metricCount = 0;

            for (com.football_club.Scouting.model.SeasonalValuedMetric vm : report.getCustomMetrics()) {
                if (vm.getPercentile() != null) {
                    totalPercentile += vm.getPercentile();
                    metricCount++;
                }
            }

            // Average the percentiles, then multiply by league difficulty to get the final score
            double averagePercentile = metricCount > 0 ? (totalPercentile / metricCount) : 0.0;
            double finalScore = averagePercentile * report.getLeague().getDifficultyMultiplier();

            recommendations.add(new PlayerRecommendationDTO(
                    report.getPlayer().getId(),
                    report.getPlayer().getName(),
                    report.getPlayer().getSurname(),
                    finalScore,
                    "CAMPAIGN_AVERAGE"
            ));
        }

        return recommendations.stream()
                .sorted(Comparator.comparingDouble(PlayerRecommendationDTO::getScore).reversed())
                .collect(Collectors.toList());
    }


}