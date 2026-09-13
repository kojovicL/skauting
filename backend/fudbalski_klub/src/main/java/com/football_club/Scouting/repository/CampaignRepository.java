package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Campaign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {
    List<Campaign> findByDirectorId(Long directorId);

    @Query("SELECT c.director.id FROM Campaign c JOIN c.monitoredPlayers mp WHERE mp.player.id = :playerId")
    List<Long> findDirectorIdsByMonitoredPlayerId(@Param("playerId") Long playerId);
}