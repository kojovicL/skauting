package com.football_club.Scouting.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDraftDataDTO {
    private MatchDraftInfo match;
    private PlayerDraftInfo player;
    private ApiMatchStatSummary apiStat;
    private List<MetricDTO> allSystemMetrics;

    @Getter @Builder
    public static class MatchDraftInfo {
        private Long matchId;
        private LocalDateTime date;
        private String status;
        private String homeTeamName;
        private String homeTeamLogo;
        private Integer homeGoals;
        private String awayTeamName;
        private String awayTeamLogo;
        private Integer awayGoals;
        private String leagueName;
        private Double difficultyMultiplier;
    }

    @Getter @Builder
    public static class PlayerDraftInfo {
        private Long playerId;
        private String name;
        private String surname;
        private String photoUrl;
        private String currentTeamName;
    }

    @Getter @Builder
    public static class ApiMatchStatSummary {
        private Integer minutesPlayed;
        private Integer shirtNumber;
        private Boolean isSubstitute;
        private Boolean isCaptain;
        private Integer goals;
        private Integer assists;
        private Double rawRating;
        private List<ApiValuedMetricDTO> matchMetrics;
    }

    @Getter @Builder
    public static class ApiValuedMetricDTO {
        private Long metricId;
        private String metricName;
        private double value;
    }
}