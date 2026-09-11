package com.football_club.Scouting.service;

import com.football_club.Scouting.model.Team;
import java.util.List;

public interface ITeamService {
    Team createTeam(Team team);
    Team getTeamById(Long id);
    List<Team> getAllTeams();
    Team updateTeam(Long id, Team team);
    void deleteTeam(Long id);
}