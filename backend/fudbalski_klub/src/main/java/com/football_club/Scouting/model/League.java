package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class League {

    @Id
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "difficulty_multiplier", nullable = false)
    private double difficultyMultiplier;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    private List<Team> teams;

    public void addTeam(Team team) {
        this.teams.add(team);
        team.setLeague(this);
    }

    public void removeTeam(Team team) {
        this.teams.remove(team);
        team.setLeague(null);
    }
}