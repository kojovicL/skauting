package com.football_club.Scouting.model;

import com.football_club.Scouting.model.Match;
import com.football_club.Scouting.model.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "game_metric_values", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"game_id", "player_id", "metric_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", nullable = false)
    private Metric metric;

    @Column(name = "recorded_value", nullable = false)
    private double recordedValue;
}
