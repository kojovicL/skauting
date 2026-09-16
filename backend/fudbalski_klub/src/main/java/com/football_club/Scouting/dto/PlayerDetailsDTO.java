package com.football_club.Scouting.dto;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PlayerDetailsDTO {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String nationality;
    private String photoUrl;
    private String position;
    private String currentTeamName;
    private String currentTeamLogo;
}