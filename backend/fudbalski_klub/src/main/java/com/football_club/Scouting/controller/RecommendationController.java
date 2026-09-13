package com.football_club.Scouting.controller;

import com.football_club.Scouting.dto.PlayerRecommendationDTO;
import com.football_club.Scouting.dto.RecommendationRequestDTO;
import com.football_club.Scouting.service.IRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final IRecommendationService recommendationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<PlayerRecommendationDTO>> getRecommendations(
            @RequestBody RecommendationRequestDTO request
            ) {
        if (request == null || request.getPosition() == null || request.getMetricWeights() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<PlayerRecommendationDTO> result = recommendationService.getRecommendations(
                request.getPosition(),
                request.getMetricWeights()
                        .stream()
                        .collect(Collectors
                                .toMap(RecommendationRequestDTO.WeightedMetrics::getMetricId,
                                        RecommendationRequestDTO.WeightedMetrics::getWeight))
        );
        return ResponseEntity.ok(result);
    }
}
