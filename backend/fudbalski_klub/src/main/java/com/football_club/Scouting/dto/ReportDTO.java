package com.football_club.Scouting.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private Long scoutId;
    private String scoutUsername;
    private LocalDateTime createdAt;
    private String overallCommentary;
    private Long teamAtTimeId;
    private String teamAtTimeName;
    private double leagueMultiplierAtTime;
    private List<ValuedMetricDTO> valuedMetrics;
}
