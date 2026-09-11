package com.football_club.Scouting.service;

import com.football_club.Scouting.model.Season;
import java.util.List;

public interface ISeasonService {
    Season createSeason(Season season);
    Season getSeasonById(Long id);
    List<Season> getAllSeasons();
    Season updateSeason(Long id, Season season);
    void deleteSeason(Long id);
}