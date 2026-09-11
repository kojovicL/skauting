package com.football_club.Scouting.model;

import com.football_club.Auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "scout_reports", indexes = {
        @Index(name = "idx_report_player_date", columnList = "player_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scout_id", nullable = false)
    private User scout;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "overall_commentary", nullable = true)
    private String overallCommentary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_at_time_id")
    private Team teamAtTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @Column(name = "league_multiplier_at_time", nullable = false)
    private double leagueMultiplierAtTime;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
    private List<ValuedMetric> valuedMetrics;

}
