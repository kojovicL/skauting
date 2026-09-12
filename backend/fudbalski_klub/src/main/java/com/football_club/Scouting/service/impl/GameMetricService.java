package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Match;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.repository.MatchRepository;
import com.football_club.Scouting.repository.PlayerRepository;
import com.football_club.Scouting.dto.GameMetricDTO;
import com.football_club.Scouting.dto.GameMetricSaveDTO;
import com.football_club.Scouting.model.GameMetric;
import com.football_club.Scouting.model.Metric;
import com.football_club.Scouting.repository.GameMetricRepository;
import com.football_club.Scouting.repository.MetricRepository;
import com.football_club.Scouting.service.IGameMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameMetricService implements IGameMetricService {

    private final GameMetricRepository gameMetricRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MetricRepository metricRepository;

    @Override
    @Transactional
    public GameMetricDTO createGameMetric(GameMetricSaveDTO dto) {
        if (gameMetricRepository.existsByMatchIdAndPlayerIdAndMetricId(dto.getMatchId(), dto.getPlayerId(), dto.getMetricId())) {
            throw new IllegalArgumentException("Ova metrika utakmice već postoji za dati meč i igrača!");
        }

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new NoSuchElementException("Utakmica sa ID-em " + dto.getMatchId() + " nije pronađena."));

        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new NoSuchElementException("Igrač sa ID-em " + dto.getPlayerId() + " nije pronađen."));

        Metric metric = metricRepository.findById(dto.getMetricId())
                .orElseThrow(() -> new NoSuchElementException("Metrika sa ID-em " + dto.getMetricId() + " nije pronađena."));

        GameMetric gameMetric = new GameMetric();
        gameMetric.setMatch(match);
        gameMetric.setPlayer(player);
        gameMetric.setMetric(metric);
        gameMetric.setRecordedValue(dto.getRecordedValue());

        GameMetric saved = gameMetricRepository.save(gameMetric);
        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public List<GameMetricDTO> createGameMetrics(List<GameMetricSaveDTO> dtos) {
        List<GameMetricDTO> result = new ArrayList<>();
        for (GameMetricSaveDTO dto : dtos) {
            gameMetricRepository.findByMatchIdAndPlayerIdAndMetricId(dto.getMatchId(), dto.getPlayerId(), dto.getMetricId())
                    .ifPresentOrElse(
                            existing -> {
                                existing.setRecordedValue(dto.getRecordedValue());
                                result.add(mapToDTO(gameMetricRepository.save(existing)));
                            },
                            () -> result.add(createGameMetric(dto))
                    );
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public GameMetricDTO getGameMetricById(Long id) {
        GameMetric metric = gameMetricRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Metrika utakmice nije pronađena."));
        return mapToDTO(metric);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMetricDTO> getAllGameMetrics() {
        return gameMetricRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GameMetricDTO updateGameMetric(Long id, GameMetricSaveDTO dto) {
        GameMetric gameMetric = gameMetricRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Metrika utakmice nije pronađena."));

        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new NoSuchElementException("Utakmica nije pronađena."));

        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new NoSuchElementException("Igrač nije pronađen."));

        Metric metric = metricRepository.findById(dto.getMetricId())
                .orElseThrow(() -> new NoSuchElementException("Metrika nije pronađena."));

        gameMetric.setMatch(match);
        gameMetric.setPlayer(player);
        gameMetric.setMetric(metric);
        gameMetric.setRecordedValue(dto.getRecordedValue());

        return mapToDTO(gameMetricRepository.save(gameMetric));
    }

    @Override
    @Transactional
    public void deleteGameMetric(Long id) {
        if (!gameMetricRepository.existsById(id)) {
            throw new NoSuchElementException("Metrika utakmice ne postoji.");
        }
        gameMetricRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMetricDTO> getMetricsByGame(Long matchId) {
        return gameMetricRepository.findByMatchId(matchId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMetricDTO> getMetricsByPlayer(Long playerId) {
        return gameMetricRepository.findByPlayerId(playerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMetricDTO> getMetricsByGameAndPlayer(Long matchId, Long playerId) {
        return gameMetricRepository.findByMatchIdAndPlayerId(matchId, playerId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameMetricDTO> getLastFiveGamesMetrics(Long playerId) {
        return gameMetricRepository.findRecentMetricsByPlayer(playerId, PageRequest.of(0, 5)).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private GameMetricDTO mapToDTO(GameMetric metric) {
        return GameMetricDTO.builder()
                .id(metric.getId())
                .matchId(metric.getMatch().getId())
                .matchDate(metric.getMatch().getMatchDate())
                .playerId(metric.getPlayer().getId())
                .homeClubName(metric.getMatch().getHomeTeam().getName())
                .awayClubName(metric.getMatch().getAwayTeam().getName())
                .metricId(metric.getMetric().getId())
                .metricName(metric.getMetric().getName())
                .recordedValue(metric.getRecordedValue())
                .build();
    }
}