package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.model.enums.Region;
import java.util.List;
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
    private Region region;
    private List<Long> candidateApiIds;
}
