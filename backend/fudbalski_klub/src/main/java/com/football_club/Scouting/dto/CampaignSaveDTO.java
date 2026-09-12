package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.Position;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignSaveDTO {
    private String name;
    private String description;
    private Position targetPosition;
    private LocalDate startDate;
    private LocalDate endDate;
}
