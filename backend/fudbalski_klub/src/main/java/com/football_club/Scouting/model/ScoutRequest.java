package com.football_club.Scouting.model;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scout_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScoutRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id", nullable = false)
    private User director;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "scout_id", nullable = true)
    private User scout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "instructions", nullable = false)
    private String instructions;

    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
}
