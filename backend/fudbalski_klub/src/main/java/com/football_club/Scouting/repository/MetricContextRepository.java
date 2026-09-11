package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.MetricContext;
import com.football_club.Scouting.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Repository
public interface MetricContextRepository extends JpaRepository<MetricContext, Long> {
    List<MetricContext> findByPositionAndMetricIdIn(Position position, Set<Long> metricIds);
    @Modifying
    @Transactional
    @Query(nativeQuery = true, value =
            "INSERT INTO metric_context (metric_id, position, min_value, max_value, avg_value, last_updated, type) " +
            "SELECT " +
            "   vm.metric_id, " +
            "   p.position, " +
            "   MIN(vm.value) AS min_value, " +
            "   MAX(vm.value) AS max_value, " +
            "   AVG(vm.value) AS avg_value, " +
            "   CURRENT_TIMESTAMP AS last_updated, " +
            "   m.type " +
            "FROM valued_metrics vm " +
            "JOIN scout_reports r ON vm.report_id = r.id " +
            "JOIN players p ON r.player_id = p.id " +
            "JOIN metrics m ON vm.metric_id = m.id " +
            "WHERE p.position IS NOT NULL " +
            "GROUP BY vm.metric_id, p.position, m.type " +
            "ON CONFLICT (metric_id, position) " +
            "DO UPDATE SET " +
            "   min_value = EXCLUDED.min_value, " +
            "   max_value = EXCLUDED.max_value, " +
            "   avg_value = EXCLUDED.avg_value, " +
            "   last_updated = EXCLUDED.last_updated, " +
            "   type = EXCLUDED.type")
    int refreshMetricContextTable();
}
