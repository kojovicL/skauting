package com.football_club.Scouting.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingReportMatchDTO {
    private Long matchId;
    private LocalDateTime matchDate;
    private String homeTeamName;
    private String homeTeamLogo;
    private Integer homeGoals;
    private String awayTeamName;
    private String awayTeamLogo;
    private Integer awayGoals;
    private String leagueName;

    private Long playerId;
    private String playerName;
    private String playerSurname;
    private String playerPhotoUrl;
    private String playerPosition;

    private Integer minutesPlayed;
    private Double rating;
}