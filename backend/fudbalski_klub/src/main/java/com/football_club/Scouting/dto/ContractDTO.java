package com.football_club.Scouting.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractDTO {
    private Long id;
    private Long teamId;
    private String teamName;
    private String teamLogoUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String transferType;
}