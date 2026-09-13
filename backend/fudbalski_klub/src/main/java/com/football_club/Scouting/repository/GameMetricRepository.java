package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.GameMetric;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GameMetricRepository extends JpaRepository<GameMetric, Long> {
    List<GameMetric> findByMatchId(Long matchId);
    List<GameMetric> findByPlayerId(Long playerId);
    List<GameMetric> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    Optional<GameMetric> findByMatchIdAndPlayerIdAndMetricId(Long matchId, Long playerId, Long metricId);
    boolean existsByMatchIdAndPlayerIdAndMetricId(Long matchId, Long playerId, Long metricId);
    @Query("SELECT gm FROM GameMetric gm WHERE gm.player.id = :playerId ORDER BY gm.match.matchDate DESC")
    List<GameMetric> findRecentMetricsByPlayer(@Param("playerId") Long playerId, Pageable pageable);
}
