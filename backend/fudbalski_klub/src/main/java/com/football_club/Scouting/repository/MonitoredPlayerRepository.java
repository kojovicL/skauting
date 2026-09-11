package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.MonitoredPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MonitoredPlayerRepository extends JpaRepository<MonitoredPlayer, Long> {
    List<MonitoredPlayer> findByCampaignId(Long campaignId);
    List<MonitoredPlayer> findByPlayerId(Long playerId);

    @Modifying
    @Transactional
    @Query("UPDATE MonitoredPlayer m SET m.teamId = :newTeamId WHERE m.player.id = :playerId")
    void updateTeamIdForPlayer(@Param("playerId") Long playerId, @Param("newTeamId") Long newTeamId);
}