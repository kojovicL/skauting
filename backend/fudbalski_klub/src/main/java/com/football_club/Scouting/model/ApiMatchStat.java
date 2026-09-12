package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "api_match_stats",
        indexes = {
                @Index(name = "idx_api_stat_player_match", columnList = "player_id, match_id", unique = true)
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMatchStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // General Match Details
    private Integer minutesPlayed;
    private Integer shirtNumber;
    private Boolean isSubstitute;
    private Boolean isCaptain;

    // Core Summary Fields
    private Integer goals;
    private Integer assists;

    // Normalized Ratings
    @Column(nullable = false)
    private Double rawRating;

    @Column(nullable = false)
    private Double weightedRating; // rawRating * match.getLeague().getDifficultyMultiplier()

    // Dynamic Metrics
    @OneToMany(mappedBy = "apiMatchStat", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApiMatchValuedMetric> matchMetrics = new ArrayList<>();
}