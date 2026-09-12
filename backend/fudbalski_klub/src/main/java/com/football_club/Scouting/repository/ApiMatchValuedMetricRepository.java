package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.ApiMatchValuedMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiMatchValuedMetricRepository extends JpaRepository<ApiMatchValuedMetric, Long> {
    List<ApiMatchValuedMetric> findByApiMatchStatId(Long apiMatchStatId);
}