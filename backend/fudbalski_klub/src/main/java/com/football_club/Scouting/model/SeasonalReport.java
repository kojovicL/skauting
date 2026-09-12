package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seasonal_reports", indexes = {
        @Index(name = "idx_seasonal_player_year", columnList = "player_id, seasonYear")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(nullable = false)
    private Integer seasonYear;

    @Column(nullable = false)
    private Integer minutesPlayed;

    @Column(nullable = false)
    private double goalsPer90;

    @Column(nullable = false)
    private double assistsPer90;

    @Column(nullable = false)
    private double avgWeightedRating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportSource source;

    @OneToMany(mappedBy = "seasonalReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeasonalValuedMetric> customMetrics = new ArrayList<>();

    private Double avgScoutScore;

    public enum ReportSource {
        API_HISTORICAL,
        INTERNAL_CALCULATED
    }
}