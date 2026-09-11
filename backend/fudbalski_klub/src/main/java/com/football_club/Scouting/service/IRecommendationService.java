package com.football_club.Scouting.service;

import com.football_club.Scouting.model.enums.Position;
import com.football_club.Scouting.dto.PlayerRecommendationDTO;

import java.util.List;
import java.util.Map;

public interface IRecommendationService {
    List<PlayerRecommendationDTO> getRecommendations(Position position, Map<Long, Double> metricWeights);
}
