package com.football_club.Scouting.controller;

import com.football_club.Scouting.service.impl.MatchProcessingCronService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class CronTestController {

    private final MatchProcessingCronService cronService;

    @PostMapping("/trigger-match-cron")
    @PreAuthorize("hasAnyRole('SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<Map<String, String>> triggerMatchCron() {
        cronService.processDailyMatches();
        return ResponseEntity.ok(Map.of("message", "Daily match processing executed successfully."));
    }
}