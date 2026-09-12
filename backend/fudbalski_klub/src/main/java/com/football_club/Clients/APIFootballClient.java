package com.football_club.Clients;

import com.football_club.dto.apifootball.PlayerProfileResponse;
import com.football_club.dto.apifootball.TeamSearch;
import com.football_club.dto.apifootball.PlayerSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


@Service
@RequiredArgsConstructor
public class APIFootballClient {
    private static final Logger log = LoggerFactory.getLogger(APIFootballClient.class);
    private final RestClient restClient;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Search for a team by name
    public TeamSearch searchTeams(String teamName) {
        return restClient.get()
                .uri("/teams?search={name}", teamName)
                .retrieve()
                .body(TeamSearch.class);
    }

    // Search for a player by name
    public PlayerProfileResponse searchPlayers(String name) {
        return restClient.get()
                .uri("/players/profiles?search={name}", name)
                .retrieve()
                .body(PlayerProfileResponse.class);
    }

    // Search for a player by ID and season
    public PlayerSearchResponse getPlayerByIdAndSeason(Long id, Integer season) {
        String json = restClient.get()
                .uri("/players?id={id}&season={season}", id, season)
                .retrieve()
                .body(String.class);
        log.info("APIFootball getPlayerByIdAndSeason response: {}", json);
        try {
            JsonNode rootNode = objectMapper.readTree(json);
            JsonNode errorsNode = rootNode.get("errors");

            // API-Football returns errors either as an empty array [] or as an object {"errorKey": "message"}
            if (errorsNode != null && errorsNode.isObject() && !errorsNode.isEmpty()) {
                throw new RuntimeException("API-Football error: " + errorsNode.toString());
            }

            return objectMapper.treeToValue(rootNode, PlayerSearchResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse PlayerSearchResponse. Raw body: {}", json, e);
            throw new RuntimeException("API-Football response parsing error: " + e.getMessage(), e);
        }
    }
}
