package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.RoleEnum;
import com.football_club.Auth.model.User;
import com.football_club.Auth.repository.UserRepository;
import com.football_club.Scouting.dto.ScoutActivePlayerDTO;
import com.football_club.Scouting.dto.ScoutPerformanceDTO;
import com.football_club.Scouting.model.MonitoredPlayer;
import com.football_club.Scouting.repository.MonitoredPlayerRepository;
import com.football_club.Scouting.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoutManagementService {

    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final MonitoredPlayerRepository monitoredPlayerRepository;

    @Transactional(readOnly = true)
    public List<ScoutPerformanceDTO> getScoutPerformances() {
        List<User> scouts = userRepository.findByRole(RoleEnum.ROLE_SCOUT);

        return scouts.stream().map(scout -> ScoutPerformanceDTO.builder()
                .id(scout.getId())
                .name(scout.getName())
                .surname(scout.getSurname())
                .username(scout.getUsername())
                .email(scout.getEmail())
                .region(scout.getRegion())
                .totalReports(reportRepository.countByScoutId(scout.getId()))
                .activeMonitoredPlayers(monitoredPlayerRepository.countActiveByScoutId(scout.getId()))
                .build()
        ).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScoutActivePlayerDTO> getActivePlayersForScout(Long scoutId) {
        List<MonitoredPlayer> activePlayers = monitoredPlayerRepository.findActiveByScoutId(scoutId);

        return activePlayers.stream().map(mp -> ScoutActivePlayerDTO.builder()
                .playerId(mp.getPlayer().getId())
                .playerName(mp.getPlayer().getName())
                .playerSurname(mp.getPlayer().getSurname())
                .photoUrl(mp.getPlayer().getPhotoUrl())
                .campaignName(mp.getCampaign().getName())
                .currentTeamName(mp.getPlayer().getCurrentTeam() != null ? mp.getPlayer().getCurrentTeam().getName() : "Slobodan igrač")
                .build()
        ).collect(Collectors.toList());
    }
}