package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMetricSaveDTO {
    private Long matchId;
    private Long playerId;
    private Long metricId;
    private double recordedValue;
}
