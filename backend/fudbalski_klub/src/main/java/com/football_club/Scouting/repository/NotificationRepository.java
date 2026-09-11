package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByScoutIdOrderByCreatedAtDesc(Long scoutId);
    List<Notification> findByScoutIdAndIsReadFalse(Long scoutId);
}