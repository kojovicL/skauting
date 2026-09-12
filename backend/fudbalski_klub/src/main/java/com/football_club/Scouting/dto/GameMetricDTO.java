package com.football_club.Scouting.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameMetricDTO {
    private Long id;
    private Long matchId;
    private String homeClubName;
    private String awayClubName;
    private LocalDateTime matchDate;
    private Long playerId;
    private Long metricId;
    private String metricName;
    private double recordedValue;
}
