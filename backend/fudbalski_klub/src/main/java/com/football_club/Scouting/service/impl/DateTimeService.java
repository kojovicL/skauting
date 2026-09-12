package com.football_club.Scouting.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class DateTimeService {

    @Value("${scouting.simulation.current-date:2024-05-18}")
    private String currentDateStr;

    public LocalDate getCurrentDate() {
        return LocalDate.parse(currentDateStr);
    }

    public LocalDate getYesterday() {
        return getCurrentDate().minusDays(1);
    }

    public LocalDate getInSevenDays() {
        return getCurrentDate().plusDays(7);
    }

    public Integer getCurrentSeasonYear() {
        return getCurrentDate().getYear();
    }
}