package com.football_club.Scouting.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpcomingMatchTaskDTO {
    private Long matchId;
    private LocalDateTime matchDate;
    private String homeTeamName;
    private String homeTeamLogo;
    private String awayTeamName;
    private String awayTeamLogo;
    private String leagueName;
    private List<ScoutMatchPlayerDTO> players;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScoutMatchPlayerDTO {
        private Long playerId;
        private String name;
        private String surname;
        private String photoUrl;
        private String teamName;
    }
}