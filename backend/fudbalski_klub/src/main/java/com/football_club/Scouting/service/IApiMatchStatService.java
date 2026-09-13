package com.football_club.Scouting.service;

import com.football_club.Scouting.model.ApiMatchStat;
import java.util.List;

public interface IApiMatchStatService {
    ApiMatchStat createApiMatchStat(ApiMatchStat stat);
    ApiMatchStat getApiMatchStatById(Long id);
    List<ApiMatchStat> getAllApiMatchStats();
    void deleteApiMatchStat(Long id);
}