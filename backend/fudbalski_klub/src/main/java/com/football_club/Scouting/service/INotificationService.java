package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.NotificationDTO;
import com.football_club.Scouting.model.Notification;
import java.util.List;

public interface INotificationService {
    NotificationDTO createNotification(Notification notification);
    NotificationDTO getNotificationById(Long id);
    List<NotificationDTO> getNotificationsForScout(Long scoutId);
    List<NotificationDTO> getUnreadNotificationsForScout(Long scoutId);
    NotificationDTO markAsRead(Long id);
    void deleteNotification(Long id);
}