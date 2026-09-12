package com.football_club.Scouting.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "api_match_valued_metrics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"api_match_stat_id", "metric_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiMatchValuedMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_match_stat_id", nullable = false)
    private ApiMatchStat apiMatchStat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "metric_id", nullable = false)
    private Metric metric;

    @Column(nullable = false)
    private double value;
}