package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.dto.LeagueDTO;
import com.football_club.Scouting.dto.LeagueSaveDTO;
import com.football_club.Scouting.model.League;
import com.football_club.Scouting.repository.LeagueRepository;
import com.football_club.Scouting.service.ILeagueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.repository.TeamRepository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeagueService implements ILeagueService {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public LeagueDTO createLeague(LeagueSaveDTO dto) {
        League league = new League();
        league.setName(dto.getName());
        league.setDifficultyMultiplier(dto.getDifficultyMultiplier());

        League savedLeague = leagueRepository.save(league);
        return mapToDTO(savedLeague);
    }

    @Override
    @Transactional(readOnly = true)
    public LeagueDTO getLeagueById(Long id) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liga sa ID-em " + id + " nije pronađena."));
        return mapToDTO(league);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeagueDTO> getAllLeagues() {
        return leagueRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LeagueDTO updateLeague(Long id, LeagueSaveDTO dto) {
        League league = leagueRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Liga sa ID-em " + id + " nije pronađena."));

        league.setName(dto.getName());
        league.setDifficultyMultiplier(dto.getDifficultyMultiplier());

        League updatedLeague = leagueRepository.save(league);
        return mapToDTO(updatedLeague);
    }

    @Override
    @Transactional
    public void deleteLeague(Long id) {
        if (!leagueRepository.existsById(id)) {
            throw new NoSuchElementException("Neuspešno brisanje. Liga sa ID-em " + id + " ne postoji.");
        }
        leagueRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addTeamToLeague(Long leagueId, Long teamId) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new NoSuchElementException("Liga sa ID-em " + leagueId + " ne postoji."));

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new NoSuchElementException("Klub sa ID-em " + teamId + " ne postoji."));

        league.addTeam(team);
        
        leagueRepository.save(league);
    }

    @Override
    @Transactional
    public void updateLeagueMultipliers(Map<Long, Double> multipliers) {
        for (Map.Entry<Long, Double> entry : multipliers.entrySet()) {
            leagueRepository.findById(entry.getKey()).ifPresent(league -> {
                league.setDifficultyMultiplier(entry.getValue());
                leagueRepository.save(league);
            });
        }
    }
    
    private LeagueDTO mapToDTO(League league) {
        return LeagueDTO.builder()
                .id(league.getId())
                .name(league.getName())
                .difficultyMultiplier(league.getDifficultyMultiplier())
                .build();
    }
}
