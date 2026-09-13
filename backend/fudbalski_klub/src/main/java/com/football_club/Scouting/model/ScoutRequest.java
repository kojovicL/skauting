package com.football_club.Scouting.model;

import com.football_club.Scouting.model.enums.RequestStatus;
import com.football_club.Scouting.model.enums.Region;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitored_player_id", nullable = false, unique = true)
    private MonitoredPlayer monitoredPlayer;

    @Column(name = "request_date", nullable = false)
    private LocalDate requestDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 50)
    @Builder.Default
    private Region region = Region.GLOBAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private RequestStatus status = RequestStatus.PENDING;
}