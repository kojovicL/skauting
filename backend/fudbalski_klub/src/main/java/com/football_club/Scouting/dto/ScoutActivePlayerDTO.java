package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoutActivePlayerDTO {
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private String photoUrl;
    private String campaignName;
    private String currentTeamName;
}