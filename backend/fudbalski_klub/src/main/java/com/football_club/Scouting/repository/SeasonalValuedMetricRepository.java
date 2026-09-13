package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.SeasonalValuedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SeasonalValuedMetricRepository extends JpaRepository<SeasonalValuedMetric, Long> {

    List<SeasonalValuedMetric> findBySeasonalReportId(Long seasonalReportId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = """
        WITH RankedMetrics AS (
            SELECT 
                svm.id AS svm_id,
                CASE 
                    WHEN m.type = 'NEGATIVE' THEN 
                        (1.0 - PERCENT_RANK() OVER (
                            PARTITION BY p.position, svm.metric_id, sr.season_year 
                            ORDER BY svm.aggregated_value ASC
                        )) * 100.0
                    ELSE 
                        PERCENT_RANK() OVER (
                            PARTITION BY p.position, svm.metric_id, sr.season_year 
                            ORDER BY svm.aggregated_value ASC
                        ) * 100.0
                END AS calculated_percentile
            FROM seasonal_valued_metrics svm
            JOIN seasonal_reports sr ON svm.seasonal_report_id = sr.id
            JOIN players p ON sr.player_id = p.id
            JOIN metrics m ON svm.metric_id = m.id
            WHERE sr.season_year = :seasonYear
              AND p.position IS NOT NULL
        )
        UPDATE seasonal_valued_metrics target
        SET percentile = ROUND(CAST(rm.calculated_percentile AS NUMERIC), 1)
        FROM RankedMetrics rm
        WHERE target.id = rm.svm_id
    """)
    int updatePercentilesForSeason(@Param("seasonYear") Integer seasonYear);

    @Query(nativeQuery = true, value = """
        SELECT DISTINCT sr.season_year
        FROM seasonal_reports sr
        JOIN seasonal_valued_metrics svm ON sr.id = svm.seasonal_report_id
        WHERE svm.percentile IS NULL
    """)
    List<Integer> findSeasonYearsWithMissingPercentiles();
}