package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerReportRepository extends JpaRepository<Player, Long> {
    @Query(value = "SELECT get_player_report_data(:playerId)", nativeQuery = true)
    String getPlayerReportJson(@Param("playerId") Long playerId);
}
