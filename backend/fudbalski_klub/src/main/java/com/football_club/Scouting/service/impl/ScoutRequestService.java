package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.RoleEnum;
import com.football_club.Auth.model.User;
import com.football_club.Auth.repository.UserRepository;
import com.football_club.Scouting.dto.ScoutRequestDTO;
import com.football_club.Scouting.model.MonitoredPlayer;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.ScoutRequest;
import com.football_club.Scouting.model.enums.RequestStatus;
import com.football_club.Scouting.repository.MonitoredPlayerRepository;
import com.football_club.Scouting.repository.ScoutRequestRepository;
import com.football_club.Scouting.service.IScoutRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoutRequestService implements IScoutRequestService {

    private final ScoutRequestRepository scoutRequestRepository;
    private final MonitoredPlayerRepository monitoredPlayerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScoutRequestDTO> getPendingRequests() {
        return scoutRequestRepository.findByStatusWithDetails(RequestStatus.PENDING).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScoutRequestDTO getRequestById(Long id) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting sa ID-em " + id + " ne postoji."));
        return mapToDTO(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoutRequestDTO> getAllRequests() {
        return scoutRequestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScoutRequestDTO claimRequest(Long id, Long scoutId) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting sa ID-em " + id + " nije pronađen."));

        // Validation: Verify status is still PENDING
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Zahtev nije u statusu PENDING i ne može se preuzeti.");
        }

        User scout = userRepository.findById(scoutId)
                .orElseThrow(() -> new NoSuchElementException("Korisnik sa ID-em " + scoutId + " nije pronađen."));

        // Validation: Verify user has the SCOUT or ADMIN role
        if (scout.getRole() != RoleEnum.ROLE_SCOUT && scout.getRole() != RoleEnum.ROLE_ADMIN) {
            throw new IllegalArgumentException("Samo skauti mogu preuzimati zahteve za posmatranje.");
        }

        MonitoredPlayer monitoredPlayer = request.getMonitoredPlayer();

        // Validation: Monitored player must not already have an assigned scout
        if (monitoredPlayer.getScout() != null) {
            throw new IllegalStateException("Ovaj igrač je već dodeljen drugom skautu.");
        }

        // Set the scout in MonitoredPlayer
        monitoredPlayer.setScout(scout);
        monitoredPlayerRepository.save(monitoredPlayer);

        // Transition status to CLAIMED
        request.setStatus(RequestStatus.CLAIMED);
        ScoutRequest saved = scoutRequestRepository.save(request);

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        if (!scoutRequestRepository.existsById(id)) {
            throw new NoSuchElementException("Zahtev za skauting ne postoji.");
        }
        scoutRequestRepository.deleteById(id);
    }

    private ScoutRequestDTO mapToDTO(ScoutRequest request) {
        MonitoredPlayer mp = request.getMonitoredPlayer();
        Player p = mp.getPlayer();
        User assignedScout = mp.getScout();

        return ScoutRequestDTO.builder()
                .id(request.getId())
                .campaignId(request.getCampaign().getId())
                .campaignName(request.getCampaign().getName())
                .monitoredPlayerId(mp.getId())
                .playerId(p.getId())
                .playerName(p.getName())
                .playerSurname(p.getSurname())
                .photoUrl(p.getPhotoUrl())
                .position(p.getPosition())
                .currentTeamName(p.getCurrentTeam() != null ? p.getCurrentTeam().getName() : "Slobodan igrač")
                .playerAge(p.getAge())
                .requestDate(request.getRequestDate())
                .status(request.getStatus())
                .scoutId(assignedScout != null ? assignedScout.getId() : null)
                .scoutUsername(assignedScout != null ? assignedScout.getUsername() : null)
                .build();
    }
}