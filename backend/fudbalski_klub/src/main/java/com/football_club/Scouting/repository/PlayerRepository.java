package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Player;
import com.football_club.Scouting.model.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByCurrentTeamId(Long teamId);
    List<Player> findByPosition(Position position);
    @Query("SELECT p FROM Player p WHERE LOWER(p.surname) LIKE LOWER(CONCAT('%', :surname, '%')) OR LOWER(p.name) LIKE LOWER(CONCAT('%', :surname, '%'))")
    List<Player> searchBySurnameOrName(@Param("surname") String surname);
}