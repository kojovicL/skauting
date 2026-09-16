package com.football_club.Scouting.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerSearchDTO {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String nationality;
    private String photoUrl;
    private String position;
}