package com.football_club.Scouting.dto;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RecentMatchDTO {
    private Long matchId;
    private LocalDateTime matchDate;
    private String homeTeamName;
    private String homeTeamLogo;
    private Integer homeGoals;
    private String awayTeamName;
    private String awayTeamLogo;
    private Integer awayGoals;
    private Integer minutesPlayed;
    private Double rating;
}