package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.ApiMatchStat;
import com.football_club.Scouting.repository.ApiMatchStatRepository;
import com.football_club.Scouting.service.IApiMatchStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ApiMatchStatService implements IApiMatchStatService {

    private final ApiMatchStatRepository statRepository;

    @Override
    @Transactional
    public ApiMatchStat createApiMatchStat(ApiMatchStat stat) {
        return statRepository.save(stat);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiMatchStat getApiMatchStatById(Long id) {
        return statRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Statistika sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiMatchStat> getAllApiMatchStats() {
        return statRepository.findAll();
    }

    @Override
    @Transactional
    public void deleteApiMatchStat(Long id) {
        if (!statRepository.existsById(id)) {
            throw new NoSuchElementException("Statistika sa ID-em " + id + " ne postoji.");
        }
        statRepository.deleteById(id);
    }
}