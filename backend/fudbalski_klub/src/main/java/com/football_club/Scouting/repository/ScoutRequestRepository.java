package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.ScoutRequest;
import com.football_club.Scouting.model.enums.RequestStatus;
import com.football_club.Scouting.model.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoutRequestRepository extends JpaRepository<ScoutRequest, Long> {

    @Query("""
        SELECT sr FROM ScoutRequest sr
        JOIN FETCH sr.campaign c
        JOIN FETCH sr.monitoredPlayer mp
        JOIN FETCH mp.player p
        LEFT JOIN FETCH p.currentTeam
        LEFT JOIN FETCH mp.scout
        WHERE sr.status = :status
        ORDER BY sr.requestDate DESC
    """)
    List<ScoutRequest> findByStatusWithDetails(@Param("status") RequestStatus status);

    List<ScoutRequest> findByCampaignId(Long campaignId);

    Optional<ScoutRequest> findByMonitoredPlayerId(Long monitoredPlayerId);

    boolean existsByMonitoredPlayerId(Long monitoredPlayerId);

    @Query("""
        SELECT sr FROM ScoutRequest sr
        JOIN FETCH sr.campaign c
        JOIN FETCH sr.monitoredPlayer mp
        JOIN FETCH mp.player p
        LEFT JOIN FETCH p.currentTeam
        LEFT JOIN FETCH mp.scout
        WHERE sr.status = :status AND sr.region = :region
        ORDER BY sr.requestDate DESC
    """)
    List<ScoutRequest> findByRegionAndStatusWithDetails(@Param("region") Region region, @Param("status") RequestStatus status);

    @Query("""
        SELECT sr FROM ScoutRequest sr
        JOIN FETCH sr.campaign c
        JOIN FETCH sr.monitoredPlayer mp
        JOIN FETCH mp.player p
        LEFT JOIN FETCH p.currentTeam
        LEFT JOIN FETCH mp.scout
        WHERE sr.region = :region
        ORDER BY sr.requestDate DESC
    """)
    List<ScoutRequest> findByRegionWithDetails(@Param("region") Region region);
}