package com.football_club.Clients;

import com.football_club.dto.apifootball.playerprofile.PlayerProfileResponse;
import com.football_club.dto.apifootball.teamsearch.TeamSearch;
import com.football_club.dto.apifootball.playersearch.PlayerSearchResponse;
import com.football_club.dto.apifootball.fixtures.FixturesResponse;
import com.football_club.dto.apifootball.playerstats.PlayerStats;
import com.football_club.dto.apifootball.transfersresponse.TransfersResponse;
import java.time.LocalDate;
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
            if (errorsNode != null && errorsNode.isObject() && !errorsNode.isEmpty()) {
                throw new RuntimeException("API-Football error: " + errorsNode.toString());
            }
            return objectMapper.treeToValue(rootNode, PlayerSearchResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse PlayerSearchResponse. Raw body: {}", json, e);
            throw new RuntimeException("API-Football response parsing error: " + e.getMessage(), e);
        }
    }

    public FixturesResponse getTeamFixtures(Long teamId, LocalDate from, LocalDate to, Integer season) {
        String json = restClient.get()
                .uri("/fixtures?team={team}&from={from}&to={to}&season={season}", teamId, from.toString(), to.toString(), season)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readValue(json, FixturesResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse FixturesResponse for team {}", teamId, e);
            throw new RuntimeException("API-Football fixtures parsing error", e);
        }
    }

    public PlayerStats getFixturePlayerStats(Long fixtureId, Long teamId) {
        String json = restClient.get()
                .uri("/fixtures/players?fixture={fixture}&team={team}", fixtureId, teamId)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readValue(json, PlayerStats.class);
        } catch (Exception e) {
            log.error("Failed to parse PlayerStats for fixture {} team {}", fixtureId, teamId, e);
            throw new RuntimeException("API-Football player stats parsing error", e);
        }
    }

    public JsonNode getLeagueSeasonDetails(Long leagueId, Integer season) {
        String json = restClient.get()
                .uri("/leagues?id={id}&season={season}", leagueId, season)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.error("Failed to parse league season details for league {}", leagueId, e);
            return null;
        }
    }

    public TransfersResponse getPlayerTransfers(Long playerId) {
        String json = restClient.get()
                .uri("/transfers?player={player}", playerId)
                .retrieve()
                .body(String.class);
        try {
            return objectMapper.readValue(json, TransfersResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse transfers for player {}", playerId, e);
            return null;
        }
    }
}