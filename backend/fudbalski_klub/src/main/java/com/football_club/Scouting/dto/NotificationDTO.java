package com.football_club.Scouting.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private Long id;
    private Long scoutId;
    private Long playerId;
    private Long matchId;
    private String title;
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
}