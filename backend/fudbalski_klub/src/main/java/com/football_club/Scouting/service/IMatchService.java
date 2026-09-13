package com.football_club.Scouting.service;

import com.football_club.Scouting.model.Match;
import java.util.List;

public interface IMatchService {
    Match createMatch(Match match);
    Match getMatchById(Long id);
    List<Match> getAllMatches();
    Match updateMatch(Long id, Match match);
    void deleteMatch(Long id);
}