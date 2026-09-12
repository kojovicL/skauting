package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.model.enums.RequestStatus;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoutRequestDTO {
    private Long id;
    private Long campaignId;
    private String campaignName;
    private Long monitoredPlayerId;
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private String photoUrl;
    private Position position;
    private String currentTeamName;
    private Integer playerAge;
    private LocalDate requestDate;
    private RequestStatus status;
    private Long scoutId;
    private String scoutUsername;
}