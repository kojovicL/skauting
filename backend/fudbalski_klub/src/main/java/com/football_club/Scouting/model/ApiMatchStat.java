package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.*;

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

    // Offensive Output
    private Integer goals;
    private Integer assists;
    private Integer shotsTotal;
    private Integer shotsOnTarget;

    // Passing & Playmaking
    private Integer totalPasses;
    private Integer keyPasses;
    private Double passAccuracy;

    // Defensive Actions
    private Integer tackles;
    private Integer blocks;
    private Integer interceptions;

    // Duels & 1v1 Situations
    private Integer totalDuels;
    private Integer duelsWon;
    private Integer dribbleAttempts;
    private Integer successfulDribbles;
    private Integer dribbledPast;

    // Discipline & Fouls
    private Integer foulsCommitted;
    private Integer foulsDrawn;
    private Integer yellowCards;
    private Integer redCards;

    // Goalkeeping (Nullable for outfield players)
    private Integer saves;
    private Integer goalsConceded;

    // Penalty Tracking
    private Integer penaltiesWon;
    private Integer penaltiesCommitted;
    private Integer penaltiesScored;
    private Integer penaltiesMissed;
    private Integer penaltiesSaved;

    // Normalized Ratings
    @Column(nullable = false)
    private Double rawRating;

    @Column(nullable = false)
    private Double weightedRating; // rawRating * match.getLeague().getDifficultyMultiplier()
}