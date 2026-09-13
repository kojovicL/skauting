package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Match;
import com.football_club.Scouting.repository.MatchRepository;
import com.football_club.Scouting.service.IMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MatchService implements IMatchService {

    private final MatchRepository matchRepository;

    @Override
    @Transactional
    public Match createMatch(Match match) {
        return matchRepository.save(match);
    }

    @Override
    @Transactional(readOnly = true)
    public Match getMatchById(Long id) {
        return matchRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Utakmica sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Match> getAllMatches() {
        return matchRepository.findAll();
    }

    @Override
    @Transactional
    public Match updateMatch(Long id, Match matchDetails) {
        Match match = getMatchById(id);
        match.setMatchDate(matchDetails.getMatchDate());
        match.setStatus(matchDetails.getStatus());
        match.setHomeGoals(matchDetails.getHomeGoals());
        match.setAwayGoals(matchDetails.getAwayGoals());
        return matchRepository.save(match);
    }

    @Override
    @Transactional
    public void deleteMatch(Long id) {
        if (!matchRepository.existsById(id)) {
            throw new NoSuchElementException("Utakmica sa ID-em " + id + " ne postoji.");
        }
        matchRepository.deleteById(id);
    }
}