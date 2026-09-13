package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OnboardPlayerRequest {
    private Long apiPlayerId;
    private Long campaignId;
    private Integer season; // Optional: Defaults to current year if null
}
