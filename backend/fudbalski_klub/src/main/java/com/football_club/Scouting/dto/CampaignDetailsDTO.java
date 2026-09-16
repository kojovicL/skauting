package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.CampaignStatus;
import com.football_club.Scouting.model.enums.Region;
import com.football_club.Scouting.model.enums.Position;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class CampaignDetailsDTO {
    private Long id;
    private String name;
    private String description;
    private String targetPosition;
    private CampaignStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long directorId;
    private Region region;
    private List<MonitoredPlayerBasicDTO> monitoredPlayers;

    @Getter
    @Setter
    @Builder
    public static class MonitoredPlayerBasicDTO {
        private Long playerId;
        private String name;
        private String surname;
        private String photoUrl;
        private String currentTeamName;
        private Integer age;
    }
}