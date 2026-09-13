package com.football_club.Scouting.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalSummaryRequestDTO {
    private Long seasonalReportId;
    private String playerName;
    private String leagueName;
    private Integer seasonYear;
    private List<String> matchCommentaries;
}