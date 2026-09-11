package com.football_club.Scouting.service;

import com.football_club.Scouting.model.SeasonalValuedMetric;
import java.util.List;

public interface ISeasonalValuedMetricService {
    SeasonalValuedMetric createSeasonalValuedMetric(SeasonalValuedMetric metric);
    SeasonalValuedMetric getSeasonalValuedMetricById(Long id);
    List<SeasonalValuedMetric> getAllSeasonalValuedMetrics();
    void deleteSeasonalValuedMetric(Long id);
}