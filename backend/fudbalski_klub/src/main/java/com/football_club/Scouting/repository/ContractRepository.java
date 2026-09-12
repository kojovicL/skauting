package com.football_club.Scouting.repository;

import com.football_club.Scouting.model.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Query("""
        SELECT c FROM Contract c
        JOIN FETCH c.team
        WHERE c.player.id = :playerId
        ORDER BY c.startDate DESC
    """)
    List<Contract> findByPlayerIdWithTeams(@Param("playerId") Long playerId);

    boolean existsByPlayerId(Long playerId);
}