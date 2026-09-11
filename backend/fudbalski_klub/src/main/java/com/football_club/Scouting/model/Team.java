package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "teams")
public class Team {
    @Id
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 10)
    private String code;

    private String country;

    private Integer founded;

    private boolean national;

    private String logoUrl;

    private String venueName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "league_id")
    private League league;
}