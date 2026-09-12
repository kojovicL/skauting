package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByMatchDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Match> findByHomeTeamIdOrAwayTeamId(Long homeTeamId, Long awayTeamId);

    @Query("""
        SELECT m FROM Match m 
        WHERE (m.homeTeam.id = :team1Id AND m.awayTeam.id = :team2Id) 
           OR (m.homeTeam.id = :team2Id AND m.awayTeam.id = :team1Id)
    """)
    List<Match> findHeadToHeadMatches(@Param("team1Id") Long team1Id, @Param("team2Id") Long team2Id);

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE m.matchDate BETWEEN :startDate AND :endDate
    """)
    List<Match> findMatchesWithTeamsInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE m.matchDate >= :now 
        ORDER BY m.matchDate ASC
    """)
    List<Match> findUpcomingMatches(@Param("now") LocalDateTime now);

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE m.status = 'SCHEDULED' 
        ORDER BY m.matchDate ASC
    """)
    List<Match> findScheduledMatches();

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE m.status IN ('LIVE', 'IN_PLAY', '1H', '2H', 'HT')
    """)
    List<Match> findLiveMatches();

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE m.status IN ('FINISHED', 'FT', 'AET', 'PEN') 
        ORDER BY m.matchDate DESC
    """)
    List<Match> findFinishedMatches();

    @Query("""
        SELECT m FROM Match m 
        JOIN FETCH m.homeTeam 
        JOIN FETCH m.awayTeam 
        WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id = :teamId) 
          AND m.status = :status
    """)
    List<Match> findMatchesByTeamAndStatus(@Param("teamId") Long teamId, @Param("status") String status);
}