package com.football_club.Scouting.controller;

import com.football_club.Scouting.dto.ContractDTO;
import com.football_club.Scouting.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class ContractController {

    private final ContractRepository contractRepository;

    @GetMapping("/player/{playerId}")
    @PreAuthorize("hasAnyRole('HEAD_COACH', 'ASSISTANT_COACH', 'SCOUT', 'SPORTS_DIRECTOR', 'ADMIN')")
    public ResponseEntity<List<ContractDTO>> getPlayerContracts(@PathVariable Long playerId) {
        List<ContractDTO> list = contractRepository.findByPlayerIdWithTeams(playerId).stream()
                .map(c -> ContractDTO.builder()
                        .id(c.getId())
                        .teamId(c.getTeam().getId())
                        .teamName(c.getTeam().getName())
                        .teamLogoUrl(c.getTeam().getLogoUrl())
                        .startDate(c.getStartDate())
                        .endDate(c.getEndDate())
                        .transferType(c.getTransferType())
                        .build())
                .toList();

        return ResponseEntity.ok(list);
    }
}