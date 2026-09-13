package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.ScoutRequestDTO;
import com.football_club.Auth.model.User;
import com.football_club.Scouting.model.enums.Region;
import java.util.List;

public interface IScoutRequestService {
    ScoutRequestDTO getRequestById(Long id);
    List<ScoutRequestDTO> getAllRequests();
    ScoutRequestDTO claimRequest(Long id, Long scoutId);
    void deleteRequest(Long id);
    List<ScoutRequestDTO> getPendingRequestsForUser(User user);
    List<ScoutRequestDTO> getRequestsByRegion(Region region);
}