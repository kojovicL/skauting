package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByScoutIdOrderByCreatedAtDesc(Long scoutId);
    List<Notification> findByScoutIdAndIsReadFalse(Long scoutId);

    @Modifying
    @Transactional
    @Query("""
        UPDATE Notification n 
        SET n.isRead = true 
        WHERE n.scout.id = :scoutId 
          AND n.playerId = :playerId 
          AND n.matchId = :matchId 
          AND n.isRead = false
    """)
    void markAsReadForReport(
            @Param("scoutId") Long scoutId,
            @Param("playerId") Long playerId,
            @Param("matchId") Long matchId
    );
}