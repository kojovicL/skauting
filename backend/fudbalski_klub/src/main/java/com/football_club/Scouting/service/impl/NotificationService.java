package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.dto.NotificationDTO;
import com.football_club.Scouting.model.Notification;
import com.football_club.Scouting.repository.NotificationRepository;
import com.football_club.Scouting.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationDTO createNotification(Notification notification) {
        return mapToDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationDTO getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notifikacija nije pronađena."));
        return mapToDTO(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getNotificationsForScout(Long scoutId) {
        return notificationRepository.findByScoutIdOrderByCreatedAtDesc(scoutId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotificationsForScout(Long scoutId) {
        return notificationRepository.findByScoutIdAndIsReadFalse(scoutId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NotificationDTO markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notifikacija nije pronađena."));
        notification.setRead(true);
        return mapToDTO(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new NoSuchElementException("Notifikacija ne postoji.");
        }
        notificationRepository.deleteById(id);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .scoutId(notification.getScout().getId())
                .playerId(notification.getPlayerId())
                .matchId(notification.getMatchId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}