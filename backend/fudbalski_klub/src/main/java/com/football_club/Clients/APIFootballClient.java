package com.football_club.Clients;

import com.football_club.dto.apifootball.playerprofile.PlayerProfileResponse;
import com.football_club.dto.apifootball.teamsearch.TeamSearch;
import com.football_club.dto.apifootball.playersearch.PlayerSearchResponse;
import com.football_club.dto.apifootball.fixtures.FixturesResponse;
import com.football_club.dto.apifootball.playerstats.PlayerStats;
import com.football_club.dto.apifootball.transfersresponse.TransfersResponse;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class APIFootballClient {

    private static final Logger log = LoggerFactory.getLogger(APIFootballClient.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // --- Retry Wrapper Method ---
    private <T> T executeWithRetry(Supplier<T> apiCall) {
        int maxRetries = 3;
        long delayMs = 1500; // Start with 1.5 seconds

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return apiCall.get();
            } catch (Exception ex) {
                boolean isRateLimit = false;

                // 1. Check HTTP Status 429 from Spring RestClient
                if (ex instanceof HttpClientErrorException.TooManyRequests ||
                        (ex instanceof RestClientResponseException rre && rre.getStatusCode().value() == 429)) {
                    isRateLimit = true;
                }

                // 2. Check for API-Football rate limit message in body/runtime exceptions
                if (ex.getMessage() != null && (
                        ex.getMessage().toLowerCase().contains("ratelimit") ||
                                ex.getMessage().toLowerCase().contains("too many requests") ||
                                ex.getMessage().contains("429"))) {
                    isRateLimit = true;
                }

                if (isRateLimit && attempt < maxRetries) {
                    log.warn("Rate limit hit on API-Football. Retrying in {} ms (attempt {}/{})", delayMs, attempt, maxRetries);
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("API retry interrupted", ie);
                    }
                    delayMs *= 2; // Exponential backoff (1500ms -> 3000ms -> 6000ms)
                } else {
                    throw ex;
                }
            }
        }
        throw new RuntimeException("API-Football request failed after retries.");
    }

    // --- API Methods Wrapped with Retry ---

    public TeamSearch searchTeams(String teamName) {
        return executeWithRetry(() -> restClient.get()
                .uri("/teams?search={name}", teamName)
                .retrieve()
                .body(TeamSearch.class));
    }

    public PlayerProfileResponse searchPlayers(String name) {
        return executeWithRetry(() -> restClient.get()
                .uri("/players/profiles?search={name}", name)
                .retrieve()
                .body(PlayerProfileResponse.class));
    }

    public PlayerSearchResponse getPlayerByIdAndSeason(Long id, Integer season) {
        return executeWithRetry(() -> {
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
                if (e.getMessage() != null && e.getMessage().contains("rateLimit")) {
                    throw (RuntimeException) e; // Bubble up to trigger the retry handler
                }
                log.error("Failed to parse PlayerSearchResponse. Raw body: {}", json, e);
                throw new RuntimeException("API-Football response parsing error: " + e.getMessage(), e);
            }
        });
    }

    public FixturesResponse getTeamFixtures(Long teamId, LocalDate from, LocalDate to, Integer season) {
        return executeWithRetry(() -> {
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
        });
    }

    public PlayerStats getFixturePlayerStats(Long fixtureId, Long teamId) {
        return executeWithRetry(() -> {
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
        });
    }

    public JsonNode getLeagueSeasonDetails(Long leagueId, Integer season) {
        return executeWithRetry(() -> {
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
        });
    }

    public TransfersResponse getPlayerTransfers(Long playerId) {
        return executeWithRetry(() -> {
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
        });
    }

    public TeamSearch getTeamById(Long id) {
        return executeWithRetry(() -> restClient.get()
                .uri("/teams?id={id}", id)
                .retrieve()
                .body(TeamSearch.class));
    }
}