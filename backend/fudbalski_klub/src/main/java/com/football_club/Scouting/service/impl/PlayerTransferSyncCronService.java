package com.football_club.Scouting.service.impl;

import com.football_club.Clients.APIFootballClient;
import com.football_club.Scouting.model.Contract;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.repository.ContractRepository;
import com.football_club.Scouting.repository.MonitoredPlayerRepository;
import com.football_club.Scouting.repository.PlayerRepository;
import com.football_club.Scouting.repository.TeamRepository;
import com.football_club.dto.apifootball.transfersresponse.In;
import com.football_club.dto.apifootball.transfersresponse.Transfer;
import com.football_club.dto.apifootball.transfersresponse.TransfersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerTransferSyncCronService {

    private final PlayerRepository playerRepository;
    private final ContractRepository contractRepository;
    private final TeamRepository teamRepository;
    private final MonitoredPlayerRepository monitoredPlayerRepository;
    private final APIFootballClient apiClient;

    @Scheduled(cron = "0 0 1 * * *") // Runs daily at 1:00 AM
    public void syncAllPlayerTransfers() {
        log.info("Starting daily player transfer & contract sync cron job...");

        List<Player> allPlayers = playerRepository.findAll();

        for (Player player : allPlayers) {
            try {
                syncPlayerContractsAndClub(player);
            } catch (Exception e) {
                log.error("Failed to sync transfers for player {} (ID: {}): {}",
                        player.getName(), player.getId(), e.getMessage());
            }
        }

        log.info("Daily player transfer & contract sync completed.");
    }

    @Transactional
    public void syncPlayerContractsAndClub(Player player) {
        TransfersResponse response = apiClient.getPlayerTransfers(player.getId());
        if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
            return;
        }

        List<Transfer> rawTransfers = response.getResponse().get(0).getTransfers();
        if (rawTransfers == null || rawTransfers.isEmpty()) {
            return;
        }

        // 1. Sort transfers chronologically from oldest to newest
        List<Transfer> chronological = rawTransfers.stream()
                .filter(t -> t.getTeams() != null && t.getTeams().getIn() != null && t.getTeams().getIn().getId() != null)
                .filter(t -> t.getDate() != null && !t.getDate().isBlank())
                .sorted(Comparator.comparing(t -> LocalDate.parse(t.getDate())))
                .toList();

        if (chronological.isEmpty()) {
            return;
        }

        // 2. Map existing contracts by "teamId_startDate" for O(1) matching
        List<Contract> existingContracts = contractRepository.findByPlayerIdWithTeams(player.getId());
        Map<String, Contract> existingMap = existingContracts.stream()
                .collect(Collectors.toMap(
                        c -> c.getTeam().getId() + "_" + c.getStartDate(),
                        c -> c,
                        (existing, replacement) -> existing
                ));

        // 3. Reconcile contracts and update closed intervals
        for (int i = 0; i < chronological.size(); i++) {
            Transfer entry = chronological.get(i);
            In inTeamDto = entry.getTeams().getIn();
            LocalDate startDate = LocalDate.parse(entry.getDate());
            LocalDate endDate = (i < chronological.size() - 1)
                    ? LocalDate.parse(chronological.get(i + 1).getDate())
                    : null; // The latest transfer has no endDate

            String key = inTeamDto.getId().longValue() + "_" + startDate;
            Contract contract = existingMap.get(key);

            if (contract != null) {
                // If a previously active contract has now ended due to a new transfer, update its endDate
                if (endDate != null && !endDate.equals(contract.getEndDate())) {
                    contract.setEndDate(endDate);
                    contractRepository.save(contract);
                }
            } else {
                Team team = resolveOrCreateHistoricalTeam(
                        inTeamDto.getId().longValue(),
                        inTeamDto.getName(),
                        inTeamDto.getLogo()
                );
                String transferType = entry.getType() != null ? entry.getType().toString() : "N/A";

                Contract newContract = Contract.builder()
                        .player(player)
                        .team(team)
                        .startDate(startDate)
                        .endDate(endDate)
                        .transferType(transferType)
                        .build();

                contractRepository.save(newContract);
            }
        }

        // 4. Update the player's active current club and monitored campaign references
        Transfer latestTransfer = chronological.get(chronological.size() - 1);
        In latestInTeam = latestTransfer.getTeams().getIn();
        Long latestTeamId = latestInTeam.getId().longValue();

        boolean teamChanged = player.getCurrentTeam() == null
                || !player.getCurrentTeam().getId().equals(latestTeamId);

        if (teamChanged) {
            Team newCurrentTeam = resolveOrCreateHistoricalTeam(
                    latestTeamId,
                    latestInTeam.getName(),
                    latestInTeam.getLogo()
            );

            player.setCurrentTeam(newCurrentTeam);
            playerRepository.save(player);

            // Keep denormalized team ID in sync for the match-tracking cron
            monitoredPlayerRepository.updateTeamIdForPlayer(player.getId(), newCurrentTeam.getId());

            log.info("Player {} {} moved to new club: {} (ID: {})",
                    player.getName(), player.getSurname(), newCurrentTeam.getName(), newCurrentTeam.getId());
        }
    }

    private Team resolveOrCreateHistoricalTeam(Long teamId, String name, String logo) {
        return teamRepository.findById(teamId).orElseGet(() -> {
            Team t = new Team();
            t.setId(teamId);
            t.setName(name != null ? name : "Unknown Team");
            t.setLogoUrl(logo);
            t.setLeague(null);
            return teamRepository.save(t);
        });
    }
}