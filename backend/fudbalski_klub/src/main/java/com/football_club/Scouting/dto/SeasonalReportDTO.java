package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.SeasonalReport.ReportSource;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalReportDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private String playerSurname;
    private Integer seasonYear;
    private ReportSource source;
    private Double avgScoutScore;
    private List<SeasonalValuedMetricDTO> metrics;
}