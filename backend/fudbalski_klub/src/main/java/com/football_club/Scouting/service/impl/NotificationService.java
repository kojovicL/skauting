package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Notification;
import com.football_club.Scouting.repository.NotificationRepository;
import com.football_club.Scouting.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notifikacija sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForScout(Long scoutId) {
        return notificationRepository.findByScoutIdOrderByCreatedAtDesc(scoutId);
    }

    @Override
    @Transactional
    public Notification markAsRead(Long id) {
        Notification notification = getNotificationById(id);
        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new NoSuchElementException("Notifikacija sa ID-em " + id + " ne postoji.");
        }
        notificationRepository.deleteById(id);
    }
}