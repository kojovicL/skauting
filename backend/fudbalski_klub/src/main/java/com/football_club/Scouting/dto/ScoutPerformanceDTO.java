package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.Region;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoutPerformanceDTO {
    private Long id;
    private String name;
    private String surname;
    private String username;
    private String email;
    private Region region;
    private long totalReports;
    private long activeMonitoredPlayers;
}