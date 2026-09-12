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
    interface FallbackMetricProjection {
        Long getPlayerId();
        String getName();
        String getSurname();
        Long getMetricId();
        Double getAvgValue();
    }


    List<GameMetric> findByMatchId(Long matchId);
    List<GameMetric> findByPlayerId(Long playerId);
    List<GameMetric> findByMatchIdAndPlayerId(Long matchId, Long playerId);
    Optional<GameMetric> findByMatchIdAndPlayerIdAndMetricId(Long matchId, Long playerId, Long metricId);
    boolean existsByMatchIdAndPlayerIdAndMetricId(Long matchId, Long playerId, Long metricId);
    @Query("SELECT gm FROM GameMetric gm WHERE gm.player.id = :playerId ORDER BY gm.match.matchDate DESC")
    List<GameMetric> findRecentMetricsByPlayer(@Param("playerId") Long playerId, Pageable pageable);
    @Query(nativeQuery = true, value =
            "WITH RankedGames AS ( " +
            "   SELECT gm.player_id, p.name, p.surname, gm.metric_id, gm.recorded_value, " +
            "          DENSE_RANK() OVER (PARTITION BY gm.player_id ORDER BY gm.game_id DESC) AS game_rank" +
            "   FROM game_metric_values gm " +
            "   JOIN players p ON gm.player_id = p.id " +
            "   LEFT JOIN scout_reports r ON r.player_id = p.id " +
            "   WHERE p.position = :position AND gm.metric_id IN (:metricIds) AND r.id IS NULL" +
            ") " +
            "SELECT player_id AS playerId, name, surname, metric_id AS metricId, AVG(recorded_value) AS avgValue " +
            "FROM RankedGames " +
            "WHERE game_rank <= 5 " +
            "GROUP BY player_id, name, surname, metric_id")
    List<FallbackMetricProjection> findFallbackAverages(
            @Param("position") String position,
            @Param("metricIds") Set<Long> metricIds
    );
}
