package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.MetricCategory;
import com.football_club.Scouting.model.enums.MetricType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValuedMetricDTO {
    private Long id;
    private Long reportId;
    private Long metricId;
    private String metricName;
    private MetricType type;
    private MetricCategory category;
    private double value;
}
