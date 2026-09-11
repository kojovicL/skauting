package com.football_club.Scouting.dto;

import com.football_club.Scouting.model.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationRequestDTO {
    private Position position;
    private List<WeightedMetrics> metricWeights;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeightedMetrics {
        private Long metricdId;
        private Double weight;
    }
}
