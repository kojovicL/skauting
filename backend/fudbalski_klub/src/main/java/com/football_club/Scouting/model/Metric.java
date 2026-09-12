package com.football_club.Scouting.model;

import com.football_club.Scouting.model.enums.MetricCategory;
import com.football_club.Scouting.model.enums.MetricType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.STRING)
    private MetricType type;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private MetricCategory category;
}
