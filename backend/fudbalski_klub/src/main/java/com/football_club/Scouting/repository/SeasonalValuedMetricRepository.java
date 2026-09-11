package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.SeasonalValuedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeasonalValuedMetricRepository extends JpaRepository<SeasonalValuedMetric, Long> {
    List<SeasonalValuedMetric> findBySeasonalReportId(Long seasonalReportId);
}