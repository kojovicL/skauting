package com.football_club.Scouting.service;

import com.football_club.Auth.model.User;
import com.football_club.Scouting.dto.OnboardPlayerRequest;
import com.football_club.Scouting.model.MonitoredPlayer;

public interface IPlayerOnboardingService {
    MonitoredPlayer onboardPlayer(OnboardPlayerRequest request, User currentUser);
}