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
    Optional<SeasonalReport> findByPlayerIdAndSeasonYear(Long playerId, Integer seasonYear);
    boolean existsByPlayerIdAndSeasonYear(Long playerId, Integer seasonYear);

    @Query("""
        SELECT DISTINCT sr FROM SeasonalReport sr
        LEFT JOIN FETCH sr.customMetrics cm
        LEFT JOIN FETCH cm.metric
        WHERE sr.player.id = :playerId
        ORDER BY sr.seasonYear DESC
    """)
    List<SeasonalReport> findByPlayerIdWithMetrics(@Param("playerId") Long playerId);
}