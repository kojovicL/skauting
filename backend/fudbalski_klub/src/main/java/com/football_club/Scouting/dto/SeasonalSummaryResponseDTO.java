package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalSummaryResponseDTO {
    private Long seasonalReportId;
    private String summary;
}