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

    private Long matchId;
    private String homeTeamName;
    private String awayTeamName;

    private Integer minutesPlayed;
    private Integer shirtNumber;
    private Boolean isSubstitute;
    private Boolean isCaptain;
    private Integer goals;
    private Integer assists;
    private Double rawRating;
    private Double weightedRating;

    private List<ValuedMetricDTO> valuedMetrics;
}