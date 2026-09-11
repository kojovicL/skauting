package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    Optional<Season> findByLeagueIdAndYear(Long leagueId, Integer year);
}