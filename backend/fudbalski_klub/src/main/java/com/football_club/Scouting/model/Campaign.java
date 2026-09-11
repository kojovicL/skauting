package com.football_club.Scouting.model;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.model.enums.CampaignStatus;
import com.football_club.Scouting.model.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "campaigns")
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private Position targetPosition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate = LocalDate.now();

    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User director;

    @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MonitoredPlayer> monitoredPlayers = new ArrayList<>();

    public void addPlayer(Player player, Long teamId) {
        MonitoredPlayer monitoredPlayer = new MonitoredPlayer();
        monitoredPlayer.setPlayer(player);
        monitoredPlayer.setCampaign(this);
        monitoredPlayer.setTeamId(teamId);
        monitoredPlayers.add(monitoredPlayer);
    }

    public void removePlayer(MonitoredPlayer monitoredPlayer) {
        monitoredPlayers.remove(monitoredPlayer);
        monitoredPlayer.setCampaign(null);
    }
}