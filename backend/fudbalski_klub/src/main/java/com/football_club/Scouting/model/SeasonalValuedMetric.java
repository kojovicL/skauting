package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seasonal_valued_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"seasonal_report_id", "metric_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalValuedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seasonal_report_id", nullable = false)
    private SeasonalReport seasonalReport;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_id", nullable = false)
    private Metric metric;

    @Column(nullable = false)
    private double aggregatedValue; // Average or normalized seasonal score for this metric

    @Column(name = "percentile", nullable = true)
    private Double percentile;
}