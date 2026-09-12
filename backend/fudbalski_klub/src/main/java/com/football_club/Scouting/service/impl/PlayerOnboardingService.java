// PlayerOnboardingService.java
package com.football_club.Scouting.service.impl;

import com.football_club.Clients.APIFootballClient;
import com.football_club.Scouting.dto.OnboardPlayerRequest;
import com.football_club.Scouting.model.*;
import com.football_club.Scouting.model.League;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.repository.*;
import com.football_club.Scouting.service.IPlayerOnboardingService;
import com.football_club.dto.apifootball.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PlayerOnboardingService implements IPlayerOnboardingService {

    private final APIFootballClient apiClient;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final CampaignRepository campaignRepository;
    private final MonitoredPlayerRepository monitoredPlayerRepository;

    @Transactional
    public MonitoredPlayer onboardPlayer(OnboardPlayerRequest request) {
        int targetSeason = request.getSeason() != null ? request.getSeason() : 2024;
        PlayerSearchResponse apiResponse = apiClient.getPlayerByIdAndSeason(request.getApiPlayerId(), targetSeason);

        if (apiResponse.getResponse() == null || apiResponse.getResponse().isEmpty()) {
            throw new IllegalArgumentException("Igrač nije pronađen na API-Football za datu sezonu.");
        }

        Response__1 apiData = apiResponse.getResponse().get(0);
        Player__1 apiPlayer = apiData.getPlayer();
        Statistic apiStat = apiData.getStatistics().get(0);

        League league = leagueRepository.findById(apiStat.getLeague().getId().longValue())
                .orElseGet(() -> {
                    League l = new League();
                    l.setId(apiStat.getLeague().getId().longValue());
                    l.setName(apiStat.getLeague().getName());
                    l.setDifficultyMultiplier(1.0);
                    return leagueRepository.save(l);
                });

        Team team = teamRepository.findById(apiStat.getTeam().getId().longValue())
                .orElseGet(() -> {
                    Team t = new Team();
                    t.setId(apiStat.getTeam().getId().longValue());
                    t.setName(apiStat.getTeam().getName());
                    t.setLogoUrl(apiStat.getTeam().getLogo());
                    t.setLeague(league);
                    return teamRepository.save(t);
                });

        Player player = playerRepository.findById(apiPlayer.getId().longValue()).orElseGet(Player::new);
        player.setId(apiPlayer.getId().longValue());
        player.setName(apiPlayer.getFirstname() != null ? apiPlayer.getFirstname() : apiPlayer.getName());
        player.setSurname(apiPlayer.getLastname() != null ? apiPlayer.getLastname() : "");
        player.setAge(apiPlayer.getAge());
        player.setNationality(apiPlayer.getNationality());
        player.setPhotoUrl(apiPlayer.getPhoto());
        player.setPosition(Position.fromApiString(apiStat.getGames().getPosition()));
        player.setCurrentTeam(team);
        Player savedPlayer = playerRepository.save(player);

        Campaign campaign = campaignRepository.findById(request.getCampaignId())
                .orElseThrow(() -> new IllegalArgumentException("Kampanja ne postoji."));

        MonitoredPlayer mp = new MonitoredPlayer();
        mp.setPlayer(savedPlayer);
        mp.setCampaign(campaign);
        mp.setTeamId(team.getId());
        mp.setAddedAt(LocalDate.now());

        return monitoredPlayerRepository.save(mp);
    }
}