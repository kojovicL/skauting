package com.football_club.Scouting.service;

import com.football_club.Scouting.model.MonitoredPlayer;
import java.util.List;

public interface IMonitoredPlayerService {
    MonitoredPlayer createMonitoredPlayer(MonitoredPlayer monitoredPlayer);
    MonitoredPlayer getMonitoredPlayerById(Long id);
    List<MonitoredPlayer> getAllMonitoredPlayers();
    void deleteMonitoredPlayer(Long id);
}