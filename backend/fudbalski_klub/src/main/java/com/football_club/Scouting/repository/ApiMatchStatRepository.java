package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.ApiMatchStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiMatchStatRepository extends JpaRepository<ApiMatchStat, Long> {
    boolean existsByPlayerIdAndMatchId(Long playerId, Long matchId);
    List<ApiMatchStat> findByPlayerId(Long playerId);
    List<ApiMatchStat> findByMatchId(Long matchId);
    Optional<ApiMatchStat> findByPlayerIdAndMatchId(Long playerId, Long matchId);
}