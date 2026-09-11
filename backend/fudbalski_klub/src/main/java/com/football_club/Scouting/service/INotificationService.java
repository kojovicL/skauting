package com.football_club.Scouting.service;

import com.football_club.Scouting.model.Notification;
import java.util.List;

public interface INotificationService {
    Notification createNotification(Notification notification);
    Notification getNotificationById(Long id);
    List<Notification> getNotificationsForScout(Long scoutId);
    Notification markAsRead(Long id);
    void deleteNotification(Long id);
}