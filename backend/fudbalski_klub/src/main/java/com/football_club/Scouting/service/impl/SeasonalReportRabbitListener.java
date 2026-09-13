package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.config.RabbitMQConfig;
import com.football_club.Scouting.dto.SeasonalSummaryResponseDTO;
import com.football_club.Scouting.model.SeasonalReport;
import com.football_club.Scouting.repository.SeasonalReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeasonalReportRabbitListener {

    private final SeasonalReportRepository seasonalReportRepository;

    @RabbitListener(queues = RabbitMQConfig.SEASONAL_RESPONSE_QUEUE)
    @Transactional
    public void handleSeasonalSummaryResponse(SeasonalSummaryResponseDTO response) {
        log.info("Received LLM summary for Seasonal Report ID: {}", response.getSeasonalReportId());

        seasonalReportRepository.findById(response.getSeasonalReportId()).ifPresent(report -> {
            report.setSeasonSummary(response.getSummary());
            seasonalReportRepository.save(report);
        });
    }
}