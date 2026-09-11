package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.MonitoredPlayer;
import com.football_club.Scouting.repository.MonitoredPlayerRepository;
import com.football_club.Scouting.service.IMonitoredPlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MonitoredPlayerService implements IMonitoredPlayerService {

    private final MonitoredPlayerRepository monitoredPlayerRepository;

    @Override
    @Transactional
    public MonitoredPlayer createMonitoredPlayer(MonitoredPlayer monitoredPlayer) {
        return monitoredPlayerRepository.save(monitoredPlayer);
    }

    @Override
    @Transactional(readOnly = true)
    public MonitoredPlayer getMonitoredPlayerById(Long id) {
        return monitoredPlayerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Praćeni igrač sa ID-em " + id + " nije pronađen."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonitoredPlayer> getAllMonitoredPlayers() {
        return monitoredPlayerRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteMonitoredPlayer(Long id) {
        if (!monitoredPlayerRepository.existsById(id)) {
            throw new NoSuchElementException("Praćeni igrač sa ID-em " + id + " ne postoji.");
        }
        monitoredPlayerRepository.deleteById(id);
    }
}