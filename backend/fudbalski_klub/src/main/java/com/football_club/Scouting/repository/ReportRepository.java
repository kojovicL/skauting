package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByScoutId(Long scoutId);
    List<Report> findByPlayerId(Long playerId);
    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.valuedMetrics WHERE r.player.id = :playerId")
    List<Report> findByPlayerIdWithMetrics(@Param("playerId") Long playerId);
    @Query("SELECT r FROM Report r LEFT JOIN FETCH r.valuedMetrics WHERE r.player.id = :playerId ORDER BY r.createdAt DESC LIMIT 1")
    Report findLatestReportByPlayerId(Long playerId);
    @Query( "SELECT r FROM Report r " +
            "JOIN FETCH r.player p " +
            "JOIN FETCH r.valuedMetrics vm " +
            "WHERE p.position = :position " +
            "AND vm.metric.id IN :metricIds " +
            "AND r.createdAt = (SELECT MAX(r2.createdAt) FROM Report r2 WHERE r2.player = p)")
    List<Report> findLatestReportsWithMetrics(@Param("position")Position position,
                                              @Param("metricIds") Set<Long> metricIds);
    @Query("SELECT DISTINCT r FROM Report r " +
            "LEFT JOIN FETCH r.valuedMetrics vm " +
            "LEFT JOIN FETCH vm.metric " +
            "WHERE r.player.id IN :playerIds " +
            "AND r.createdAt = (SELECT MAX(r2.createdAt) FROM Report r2 WHERE r2.player.id = r.player.id)")
    List<Report> findLatestReportsForPlayers(@Param("playerIds") List<Long> playerIds);
    @Query("""
        SELECT r FROM Report r 
        JOIN FETCH r.player 
        JOIN FETCH r.match m 
        JOIN FETCH m.league 
        LEFT JOIN FETCH r.valuedMetrics vm 
        LEFT JOIN FETCH vm.metric 
        WHERE m.season.id = :seasonId
    """)
    List<Report> findReportsBySeasonId(@Param("seasonId") Long seasonId);
    long countByScoutId(Long scoutId);
}
