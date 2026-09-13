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

    private Long leagueId;
    private String leagueName;
    private double difficultyMultiplier;

    private Integer seasonYear;
    private ReportSource source;
    private Double avgScoutScore;
    private Integer minutesPlayed;
    private double goalsPer90;
    private double assistsPer90;
    private double avgWeightedRating;

    private List<SeasonalValuedMetricDTO> metrics;
}