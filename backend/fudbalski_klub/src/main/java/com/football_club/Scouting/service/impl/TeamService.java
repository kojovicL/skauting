package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Team;
import com.football_club.Scouting.repository.TeamRepository;
import com.football_club.Scouting.service.ITeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TeamService implements ITeamService {

    private final TeamRepository teamRepository;

    @Override
    @Transactional
    public Team createTeam(Team team) {
        return teamRepository.save(team);
    }

    @Override
    @Transactional(readOnly = true)
    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tim sa ID-em " + id + " nije pronađen."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    @Transactional
    public Team updateTeam(Long id, Team teamDetails) {
        Team team = getTeamById(id);
        team.setName(teamDetails.getName());
        team.setCode(teamDetails.getCode());
        team.setCountry(teamDetails.getCountry());
        team.setFounded(teamDetails.getFounded());
        team.setNational(teamDetails.isNational());
        team.setLogoUrl(teamDetails.getLogoUrl());
        team.setVenueName(teamDetails.getVenueName());
        if (teamDetails.getLeague() != null) {
            team.setLeague(teamDetails.getLeague());
        }
        return teamRepository.save(team);
    }

    @Override
    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new NoSuchElementException("Tim sa ID-em " + id + " ne postoji.");
        }
        teamRepository.deleteById(id);
    }
}