package com.football_club.Scouting.model;

import com.football_club.Auth.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monitored_players")
public class MonitoredPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "scout_id", nullable = true)
    private User scout;

    // Denormalized mutable team ID for high-speed batch cron processing
    @Column(nullable = false)
    private Long teamId;

    private LocalDate addedAt = LocalDate.now();
}
