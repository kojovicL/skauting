package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.repository.PlayerRepository;
import com.football_club.Scouting.service.IPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PlayerService implements IPlayerService {

    private final PlayerRepository playerRepository;

    @Override
    @Transactional
    public Player createPlayer(Player player) {
        return playerRepository.save(player);
    }

    @Override
    @Transactional(readOnly = true)
    public Player getPlayerById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Igrač sa ID-em " + id + " nije pronađen."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }

    @Override
    @Transactional
    public Player updatePlayer(Long id, Player playerDetails) {
        Player player = getPlayerById(id);
        player.setName(playerDetails.getName());
        player.setSurname(playerDetails.getSurname());
        player.setAge(playerDetails.getAge());
        player.setNationality(playerDetails.getNationality());
        player.setPhotoUrl(playerDetails.getPhotoUrl());
        player.setPosition(playerDetails.getPosition());
        player.setCurrentTeam(playerDetails.getCurrentTeam());
        return playerRepository.save(player);
    }

    @Override
    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new NoSuchElementException("Igrač sa ID-em " + id + " ne postoji.");
        }
        playerRepository.deleteById(id);
    }
}