package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.User;
import com.football_club.Auth.repository.UserRepository;
import com.football_club.Scouting.dto.*;
import com.football_club.Scouting.model.*;
import com.football_club.Scouting.model.enums.RequestStatus;
import com.football_club.Scouting.repository.*;
import com.football_club.Scouting.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService implements IReportService {

    private final ReportRepository reportRepository;
    private final PlayerRepository playerRepository;
    private final UserRepository userRepository;
    private final MatchRepository matchRepository;
    private final ApiMatchStatRepository apiMatchStatRepository;
    private final MetricRepository metricRepository;
    private final ValuedMetricRepository valuedMetricRepository;

    @Override
    @Transactional(readOnly = true)
    public ReportDraftDataDTO getReportDraftData(Long playerId, Long matchId) {
        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new NoSuchElementException("Igrač nije pronađen."));
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new NoSuchElementException("Utakmica nije pronađena."));

        ApiMatchStat apiStat = apiMatchStatRepository.findByPlayerIdAndMatchId(playerId, matchId).orElse(null);
        List<Metric> systemMetrics = metricRepository.findAll();

        return ReportDraftDataDTO.builder()
                .match(ReportDraftDataDTO.MatchDraftInfo.builder()
                        .matchId(match.getId())
                        .date(match.getMatchDate())
                        .status(match.getStatus())
                        .homeTeamName(match.getHomeTeam().getName())
                        .homeTeamLogo(match.getHomeTeam().getLogoUrl())
                        .homeGoals(match.getHomeGoals())
                        .awayTeamName(match.getAwayTeam().getName())
                        .awayTeamLogo(match.getAwayTeam().getLogoUrl())
                        .awayGoals(match.getAwayGoals())
                        .leagueName(match.getLeague().getName())
                        .difficultyMultiplier(match.getLeague().getDifficultyMultiplier())
                        .build())
                .player(ReportDraftDataDTO.PlayerDraftInfo.builder()
                        .playerId(player.getId())
                        .name(player.getName())
                        .surname(player.getSurname())
                        .photoUrl(player.getPhotoUrl())
                        .currentTeamName(player.getCurrentTeam() != null ? player.getCurrentTeam().getName() : "Slobodan igrač")
                        .build())
                .apiStat(apiStat != null ? ReportDraftDataDTO.ApiMatchStatSummary.builder()
                        .minutesPlayed(apiStat.getMinutesPlayed())
                        .shirtNumber(apiStat.getShirtNumber())
                        .isSubstitute(apiStat.getIsSubstitute())
                        .isCaptain(apiStat.getIsCaptain())
                        .goals(apiStat.getGoals())
                        .assists(apiStat.getAssists())
                        .rawRating(apiStat.getRawRating())
                        .matchMetrics(apiStat.getMatchMetrics().stream()
                                .map(m -> ReportDraftDataDTO.ApiValuedMetricDTO.builder()
                                        .metricId(m.getMetric().getId())
                                        .metricName(m.getMetric().getName())
                                        .value(m.getValue())
                                        .build())
                                .collect(Collectors.toList()))
                        .build() : null)
                .allSystemMetrics(systemMetrics.stream()
                        .map(m -> new MetricDTO(m.getId(), m.getName(), m.getCategory(), m.getType()))
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public ReportDTO createReport(ReportSaveDTO dto, Long scoutId) {
        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new NoSuchElementException("Igrač nije pronađen."));
        User scout = userRepository.findById(scoutId)
                .orElseThrow(() -> new NoSuchElementException("Skaut nije pronađen."));
        Match match = matchRepository.findById(dto.getMatchId())
                .orElseThrow(() -> new NoSuchElementException("Utakmica nije pronađena."));

        Team teamAtTime = player.getCurrentTeam();
        double multiplier = match.getLeague().getDifficultyMultiplier();
        double weightedRating = dto.getRawRating() != null ? dto.getRawRating() * multiplier : 0.0;

        Report report = new Report();
        report.setPlayer(player);
        report.setScout(scout);
        report.setMatch(match);
        report.setTeamAtTime(teamAtTime);
        report.setCreatedAt(LocalDateTime.now());
        report.setOverallCommentary(dto.getOverallCommentary());
        report.setLeagueMultiplierAtTime(multiplier);

        report.setMinutesPlayed(dto.getMinutesPlayed());
        report.setShirtNumber(dto.getShirtNumber());
        report.setIsSubstitute(dto.getIsSubstitute());
        report.setIsCaptain(dto.getIsCaptain());
        report.setGoals(dto.getGoals());
        report.setAssists(dto.getAssists());
        report.setRawRating(dto.getRawRating());
        report.setWeightedRating(weightedRating);

        Report savedReport = reportRepository.save(report);

        // Bulk insert the metrics evaluated by the scout
        if (dto.getMetrics() != null && !dto.getMetrics().isEmpty()) {
            for (ValuedMetricSaveDTO metricDto : dto.getMetrics()) {
                Metric metric = metricRepository.findById(metricDto.getMetricId())
                        .orElseThrow(() -> new NoSuchElementException("Metrika nije pronađena."));

                ValuedMetric vm = new ValuedMetric();
                vm.setReport(savedReport);
                vm.setMetric(metric);
                vm.setValue(metricDto.getValue());
                valuedMetricRepository.save(vm);
            }
        }

        return mapToDTO(savedReport);
    }

    // Keep existing methods: getReportById, getAllReports, deleteReport, getReportsByScout...

    @Override
    @Transactional
    public ReportDTO updateReport(Long id, ReportSaveDTO dto, Long scoutId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Izveštaj sa ID-em " + id + " ne postoji."));

        report.setOverallCommentary(dto.getOverallCommentary());
        report.setMinutesPlayed(dto.getMinutesPlayed());
        report.setShirtNumber(dto.getShirtNumber());
        report.setIsSubstitute(dto.getIsSubstitute());
        report.setIsCaptain(dto.getIsCaptain());
        report.setGoals(dto.getGoals());
        report.setAssists(dto.getAssists());

        if (dto.getRawRating() != null) {
            report.setRawRating(dto.getRawRating());
            report.setWeightedRating(dto.getRawRating() * report.getLeagueMultiplierAtTime());
        }

        // Sync updated metric values if provided in the update payload
        if (dto.getMetrics() != null && !dto.getMetrics().isEmpty()) {
            for (ValuedMetricSaveDTO metricDto : dto.getMetrics()) {
                valuedMetricRepository.findByReportIdAndMetricId(report.getId(), metricDto.getMetricId())
                        .ifPresentOrElse(
                                existing -> existing.setValue(metricDto.getValue()),
                                () -> {
                                    Metric metric = metricRepository.findById(metricDto.getMetricId())
                                            .orElseThrow(() -> new NoSuchElementException("Metrika nije pronađena."));
                                    ValuedMetric vm = new ValuedMetric();
                                    vm.setReport(report);
                                    vm.setMetric(metric);
                                    vm.setValue(metricDto.getValue());
                                    valuedMetricRepository.save(vm);
                                }
                        );
            }
        }

        Report updatedReport = reportRepository.save(report);
        return mapToDTO(updatedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO getReportById(Long id) {
        return mapToDTO(reportRepository.findById(id).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        return reportRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByScout(Long scoutId) {
        return reportRepository.findByScoutId(scoutId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByPlayer(Long playerId) {
        return reportRepository.findByPlayerIdWithMetrics(playerId).stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReportDTO getLatestReportByPlayer(Long playerId) {
        return mapToDTO(reportRepository.findLatestReportByPlayerId(playerId));
    }

    private ReportDTO mapToDTO(Report report) {
        List<ValuedMetricDTO> metrics = report.getValuedMetrics() != null ?
                report.getValuedMetrics().stream()
                        .map(vm -> ValuedMetricDTO.builder()
                                .id(vm.getId())
                                .reportId(report.getId())
                                .metricId(vm.getMetric().getId())
                                .metricName(vm.getMetric().getName())
                                .value(vm.getValue())
                                .build())
                        .collect(Collectors.toList()) : Collections.emptyList();

        return ReportDTO.builder()
                .id(report.getId())
                .playerId(report.getPlayer().getId())
                .playerName(report.getPlayer().getName())
                .playerSurname(report.getPlayer().getSurname())
                .scoutId(report.getScout().getId())
                .scoutUsername(report.getScout().getUsername())
                .createdAt(report.getCreatedAt())
                .overallCommentary(report.getOverallCommentary())
                .teamAtTimeId(report.getTeamAtTime() != null ? report.getTeamAtTime().getId() : null)
                .teamAtTimeName(report.getTeamAtTime() != null ? report.getTeamAtTime().getName() : null)
                .leagueMultiplierAtTime(report.getLeagueMultiplierAtTime())
                .matchId(report.getMatch() != null ? report.getMatch().getId() : null)
                .homeTeamName(report.getMatch() != null ? report.getMatch().getHomeTeam().getName() : null)
                .awayTeamName(report.getMatch() != null ? report.getMatch().getAwayTeam().getName() : null)
                .minutesPlayed(report.getMinutesPlayed())
                .shirtNumber(report.getShirtNumber())
                .isSubstitute(report.getIsSubstitute())
                .isCaptain(report.getIsCaptain())
                .goals(report.getGoals())
                .assists(report.getAssists())
                .rawRating(report.getRawRating())
                .weightedRating(report.getWeightedRating())
                .valuedMetrics(metrics)
                .build();
    }
}