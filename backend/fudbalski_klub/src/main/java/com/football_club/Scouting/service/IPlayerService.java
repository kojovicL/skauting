package com.football_club.Scouting.service;

import com.football_club.Scouting.dto.PlayerDetailsDTO;
import com.football_club.Scouting.dto.RecentMatchDTO;
import com.football_club.Scouting.model.Player;
import java.util.List;

public interface IPlayerService {
    Player createPlayer(Player player);
    Player getPlayerById(Long id);
    List<Player> getAllPlayers();
    Player updatePlayer(Long id, Player player);
    void deletePlayer(Long id);
    List<Player> searchPlayers(String surname);
    PlayerDetailsDTO getPlayerDetails(Long id);
    List<RecentMatchDTO> getRecentMatches(Long id);
}