package com.football_club.Scouting.model;

import com.football_club.Scouting.model.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "players",
        indexes = {
                @Index(name = "idx_players_name", columnList = "name"),
                @Index(name = "idx_players_current_team", columnList = "current_team_id")
        }
)
public class Player {

    @Id
    private Long id; // Direct API-Football player_id for fast O(1) primary lookups

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    private Integer age;
    private String nationality;
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "position", length = 20)
    private Position position;

    // Current club assignment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_team_id")
    private Team currentTeam;

    // Active campaign links tracking this player
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitoredPlayer> monitoredInstances = new ArrayList<>();

    // Historical and current contracts
    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contract> contracts = new ArrayList<>();

    @Transient
    public League getCurrentLeague() {
        return currentTeam != null ? currentTeam.getLeague() : null;
    }
}