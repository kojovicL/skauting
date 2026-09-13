package com.football_club.Scouting.service.impl;

import com.football_club.Scouting.model.Season;
import com.football_club.Scouting.repository.SeasonRepository;
import com.football_club.Scouting.service.ISeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SeasonService implements ISeasonService {

    private final SeasonRepository seasonRepository;

    @Override
    @Transactional
    public Season createSeason(Season season) {
        return seasonRepository.save(season);
    }

    @Override
    @Transactional(readOnly = true)
    public Season getSeasonById(Long id) {
        return seasonRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sezona sa ID-em " + id + " nije pronađena."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Season> getAllSeasons() {
        return seasonRepository.findAll();
    }

    @Override
    @Transactional
    public Season updateSeason(Long id, Season seasonDetails) {
        Season season = getSeasonById(id);
        season.setYear(seasonDetails.getYear());
        season.setStartDate(seasonDetails.getStartDate());
        season.setEndDate(seasonDetails.getEndDate());
        return seasonRepository.save(season);
    }

    @Override
    @Transactional
    public void deleteSeason(Long id) {
        if (!seasonRepository.existsById(id)) {
            throw new NoSuchElementException("Sezona sa ID-em " + id + " ne postoji.");
        }
        seasonRepository.deleteById(id);
    }
}