package com.football_club.Scouting.service.impl;

import com.football_club.Auth.model.User;
import com.football_club.Auth.repository.UserRepository;
import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.repository.PlayerRepository;
import com.football_club.Scouting.dto.ScoutRequestDTO;
import com.football_club.Scouting.dto.ScoutRequestSaveDTO;
import com.football_club.Scouting.model.ScoutRequest;
import com.football_club.Scouting.model.enums.RequestStatus;
import com.football_club.Scouting.repository.ScoutRequestRepository;
import com.football_club.Scouting.service.IScoutRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoutRequestService implements IScoutRequestService {

    private final ScoutRequestRepository scoutRequestRepository;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public ScoutRequestDTO createRequest(ScoutRequestSaveDTO dto, Long directorId) {
        User director = userRepository.findById(directorId)
                .orElseThrow(() -> new NoSuchElementException("Sportski direktor nije pronađen sa ID-em: " + directorId));

        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new NoSuchElementException("Igrač nije pronađen sa ID-em: " + dto.getPlayerId()));

        ScoutRequest request = ScoutRequest.builder()
                .director(director)
                .player(player)
                .scout(null) // Unassigned on creation
                .requestDate(LocalDateTime.now())
                .instructions(dto.getInstructions())
                .deadline(dto.getDeadline())
                .status(RequestStatus.PENDING)
                .build();

        ScoutRequest saved = scoutRequestRepository.save(request);
        return mapToDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ScoutRequestDTO getRequestById(Long id) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen sa ID-em: " + id));
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
    public ScoutRequestDTO updateRequest(Long id, ScoutRequestSaveDTO dto) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen sa ID-em: " + id));

        Player player = playerRepository.findById(dto.getPlayerId())
                .orElseThrow(() -> new NoSuchElementException("Igrač nije pronađen sa ID-em: " + dto.getPlayerId()));

        request.setPlayer(player);
        request.setInstructions(dto.getInstructions());
        request.setDeadline(dto.getDeadline());

        ScoutRequest updated = scoutRequestRepository.save(request);
        return mapToDTO(updated);
    }

    @Override
    @Transactional
    public void deleteRequest(Long id) {
        if (!scoutRequestRepository.existsById(id)) {
            throw new NoSuchElementException("Zahtev za skauting ne postoji.");
        }
        scoutRequestRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoutRequestDTO> getUnclaimedRequests() {
        return scoutRequestRepository.findByScoutIdIsNull().stream()
                .filter(status -> status.getStatus() != RequestStatus.CANCELLED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoutRequestDTO> getRequestsByScout(Long scoutId) {
        return scoutRequestRepository.findByScoutId(scoutId).stream()
                .filter(status -> status.getStatus() != RequestStatus.CANCELLED)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoutRequestDTO> getRequestsByDirector(Long directorId) {
        return scoutRequestRepository.findByDirectorId(directorId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScoutRequestDTO claimRequest(Long id, Long scoutId) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen."));

        if (request.getScout() != null) {
            throw new IllegalStateException("Ovaj zahtev je već preuzet od strane drugog skauta.");
        }

        User scout = userRepository.findById(scoutId)
                .orElseThrow(() -> new NoSuchElementException("Skaut nije pronađen sa ID-em: " + scoutId));

        request.setScout(scout);
        request.setStatus(RequestStatus.IN_PROGRESS);

        return mapToDTO(scoutRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ScoutRequestDTO cancelRequest(Long id, Long scoutId) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen."));

        User scout = userRepository.findById(scoutId)
                .orElseThrow(() -> new NoSuchElementException("Skaut nije pronađen sa ID-em: " + scoutId));

        if (!request.getScout().equals(scout)) {
            throw new IllegalStateException("Ovaj zahtev nije preuzet od strane ovog skauta.");
        }

        request.setScout(null);
        request.setStatus(RequestStatus.CANCELLED);

        return mapToDTO(scoutRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ScoutRequestDTO directorCancelRequest(Long id, Long directorId) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen."));

        User director = userRepository.findById(directorId)
                .orElseThrow(() -> new NoSuchElementException("Direktor nije pronađen sa ID-em: " + directorId));

        if (!request.getDirector().equals(director)) {
            throw new IllegalStateException("Vi niste napravili ovaj zahtev.");
        }

        request.setScout(null);
        request.setStatus(RequestStatus.CANCELLED);

        return mapToDTO(scoutRequestRepository.save(request));
    }

    @Override
    @Transactional
    public ScoutRequestDTO completeRequest(Long id) {
        ScoutRequest request = scoutRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Zahtev za skauting nije pronađen."));

        request.setStatus(RequestStatus.COMPLETED);
        return mapToDTO(scoutRequestRepository.save(request));
    }

    private ScoutRequestDTO mapToDTO(ScoutRequest request) {
        return ScoutRequestDTO.builder()
                .id(request.getId())
                .directorId(request.getDirector().getId())
                .directorName(request.getDirector().getUsername())
                .scoutId(request.getScout() != null ? request.getScout().getId() : null)
                .scoutName(request.getScout() != null ? request.getScout().getUsername() : "Unassigned")
                .playerId(request.getPlayer().getId())
                .playerName(request.getPlayer().getName())
                .playerSurname(request.getPlayer().getSurname())
                .requestDate(request.getRequestDate())
                .instructions(request.getInstructions())
                .deadline(request.getDeadline())
                .status(request.getStatus())
                .build();
    }
}
