package com.football_club.Scouting.model;

import com.football_club.Scouting.model.enums.MetricType;
import com.football_club.Scouting.model.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "metric_context", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"position", "metric_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MetricContext {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position", nullable = false)
    @Enumerated(EnumType.STRING)
    private Position position;

    @Column(name = "metric_id", nullable = false)
    private Long metricId;

    @Column(name = "min_value", nullable = false)
    private double minValue;

    @Column(name = "max_value", nullable = false)
    private double maxValue;

    @Column(name = "avg_value", nullable = false)
    private double avgValue;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.STRING)
    private MetricType type;

    public double normalize(double rawValue) {
        if (maxValue <= minValue) return 50.0;
        double normalized = 0.0;
        if (type == MetricType.POSITIVE || type == MetricType.NEUTRAL) {
            normalized = ((rawValue - minValue) / (maxValue - minValue)) * 100.0;
        }
        else {
            normalized = ((maxValue - rawValue) / (maxValue - minValue)) * 100.0;
        }
        return Math.max(0.0, Math.min(100.0, normalized));
    }
}
