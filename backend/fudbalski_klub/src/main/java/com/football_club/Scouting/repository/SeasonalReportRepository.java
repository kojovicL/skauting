package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.SeasonalReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonalReportRepository extends JpaRepository<SeasonalReport, Long> {

    List<SeasonalReport> findByPlayerId(Long playerId);

    Optional<SeasonalReport> findByPlayerIdAndSeasonYearAndLeagueId(Long playerId, Integer seasonYear, Long leagueId);

    boolean existsByPlayerIdAndSeasonYearAndLeagueId(Long playerId, Integer seasonYear, Long leagueId);

    @Query("""
        SELECT DISTINCT sr FROM SeasonalReport sr
        JOIN FETCH sr.league
        LEFT JOIN FETCH sr.customMetrics cm
        LEFT JOIN FETCH cm.metric
        WHERE sr.player.id = :playerId
        ORDER BY sr.seasonYear DESC, sr.minutesPlayed DESC
    """)
    List<SeasonalReport> findByPlayerIdWithMetrics(@Param("playerId") Long playerId);

    @Query("""
        SELECT sr FROM SeasonalReport sr 
        JOIN FETCH sr.player p 
        JOIN FETCH sr.league l 
        LEFT JOIN FETCH sr.customMetrics cm 
        LEFT JOIN FETCH cm.metric 
        WHERE p.position = :position 
          AND sr.seasonYear = (SELECT MAX(sr2.seasonYear) FROM SeasonalReport sr2 WHERE sr2.player.id = p.id)
    """)
    List<SeasonalReport> findLatestByPositionWithMetrics(@Param("position") com.football_club.Scouting.model.enums.Position position);
}