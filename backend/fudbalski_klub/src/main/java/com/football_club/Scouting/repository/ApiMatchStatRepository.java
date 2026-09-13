package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.ApiMatchStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiMatchStatRepository extends JpaRepository<ApiMatchStat, Long> {
    boolean existsByPlayerIdAndMatchId(Long playerId, Long matchId);
    List<ApiMatchStat> findByPlayerId(Long playerId);
    List<ApiMatchStat> findByMatchId(Long matchId);
    Optional<ApiMatchStat> findByPlayerIdAndMatchId(Long playerId, Long matchId);

    @Query("""
        SELECT ams FROM ApiMatchStat ams
        JOIN FETCH ams.match m
        JOIN FETCH m.homeTeam
        JOIN FETCH m.awayTeam
        JOIN FETCH m.league
        JOIN FETCH ams.player p
        JOIN p.monitoredInstances mp
        WHERE mp.scout.id = :scoutId
          AND mp.campaign.status = com.football_club.Scouting.model.enums.CampaignStatus.ACTIVE
          AND NOT EXISTS (
              SELECT r FROM Report r
              WHERE r.match.id = m.id
                AND r.player.id = p.id
                AND r.scout.id = :scoutId
          )
        ORDER BY m.matchDate DESC
    """)
    List<ApiMatchStat> findPendingUnreportedMatchesForScout(@Param("scoutId") Long scoutId);
}