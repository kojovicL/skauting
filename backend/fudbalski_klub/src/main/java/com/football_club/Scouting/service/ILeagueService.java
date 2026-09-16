package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.LeagueDTO;
import com.football_club.Scouting.dto.LeagueSaveDTO;

import java.util.List;
import java.util.Map;

public interface ILeagueService {
    LeagueDTO createLeague(LeagueSaveDTO dto);
    LeagueDTO getLeagueById(Long id);
    List<LeagueDTO> getAllLeagues();
    LeagueDTO updateLeague(Long id, LeagueSaveDTO dto);
    void deleteLeague(Long id);
    void addTeamToLeague(Long leagueId, Long teamId);
    void updateLeagueMultipliers(Map<Long, Double> multipliers);
}
