package com.football_club.Scouting.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSaveDTO {
    private Long playerId;
    private Long matchId;
    private String overallCommentary;

    // Performance Fields
    private Integer minutesPlayed;
    private Integer shirtNumber;
    private Boolean isSubstitute;
    private Boolean isCaptain;
    private Integer goals;
    private Integer assists;
    private Double rawRating;

    // Dynamic Metrics List for simultaneous creation
    private List<ValuedMetricSaveDTO> metrics;
}