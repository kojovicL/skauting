package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.ScoutRequestDTO;
import java.util.List;

public interface IScoutRequestService {
    List<ScoutRequestDTO> getPendingRequests();
    ScoutRequestDTO getRequestById(Long id);
    List<ScoutRequestDTO> getAllRequests();
    ScoutRequestDTO claimRequest(Long id, Long scoutId);
    void deleteRequest(Long id);
}