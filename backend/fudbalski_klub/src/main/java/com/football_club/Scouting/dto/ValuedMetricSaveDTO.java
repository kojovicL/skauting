package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValuedMetricSaveDTO {
    private Long metricId;
    private double value;
}
