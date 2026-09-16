package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.dto.PlayerDetailsDTO;
import com.football_club.Scouting.dto.RecentMatchDTO;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.repository.ApiMatchStatRepository;
import com.football_club.Scouting.repository.PlayerRepository;
import com.football_club.Scouting.service.IPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService {

    private final PlayerRepository playerRepository;
    private final ApiMatchStatRepository apiMatchStatRepository;

    @Override
    @Transactional
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Igrač sa ID-em " + id + " nije pronađen."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = getPlayerById(id);
        player.setName(playerDetails.getName());
        player.setSurname(playerDetails.getSurname());
        player.setAge(playerDetails.getAge());
        player.setNationality(playerDetails.getNationality());
        player.setPhotoUrl(playerDetails.getPhotoUrl());
        player.setPosition(playerDetails.getPosition());
        player.setCurrentTeam(playerDetails.getCurrentTeam());
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NoSuchElementException("Igrač sa ID-em " + id + " ne postoji.");
        }
        playerRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> searchPlayers(String surname) {
        return playerRepository.searchBySurnameOrName(surname);
    }

    @Transactional(readOnly = true)
    public PlayerDetailsDTO getPlayerDetails(Long id) {
        Player player = getPlayerById(id);
        return PlayerDetailsDTO.builder()
                .id(player.getId())
                .name(player.getName())
                .surname(player.getSurname())
                .age(player.getAge())
                .nationality(player.getNationality())
                .photoUrl(player.getPhotoUrl())
                .position(player.getPosition() != null ? player.getPosition().name() : null)
                .currentTeamName(player.getCurrentTeam() != null ? player.getCurrentTeam().getName() : "Slobodan igrač")
                .currentTeamLogo(player.getCurrentTeam() != null ? player.getCurrentTeam().getLogoUrl() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<RecentMatchDTO> getRecentMatches(Long id) {
        return apiMatchStatRepository.findByPlayerId(id).stream()
                .sorted(java.util.Comparator.comparing((com.football_club.Scouting.model.ApiMatchStat s) -> s.getMatch().getMatchDate()).reversed())
                .limit(5)
                .map(stat -> RecentMatchDTO.builder()
                        .matchId(stat.getMatch().getId())
                        .matchDate(stat.getMatch().getMatchDate())
                        .homeTeamName(stat.getMatch().getHomeTeam().getName())
                        .homeTeamLogo(stat.getMatch().getHomeTeam().getLogoUrl())
                        .homeGoals(stat.getMatch().getHomeGoals())
                        .awayTeamName(stat.getMatch().getAwayTeam().getName())
                        .awayTeamLogo(stat.getMatch().getAwayTeam().getLogoUrl())
                        .awayGoals(stat.getMatch().getAwayGoals())
                        .minutesPlayed(stat.getMinutesPlayed())
                        .rating(stat.getRawRating())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}