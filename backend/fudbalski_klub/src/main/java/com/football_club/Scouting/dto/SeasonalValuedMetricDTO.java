package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.MetricCategory;
import com.football_club.Scouting.model.enums.MetricType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalValuedMetricDTO {
    private Long id;
    private Long metricId;
    private String metricName;
    private MetricCategory category;
    private MetricType type;
    private double aggregatedValue;
    private Double percentile;
}