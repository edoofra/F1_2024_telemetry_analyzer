package com.edoofra.f1_telemetry_analyzer.persistence.repository;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LapRepository extends JpaRepository<Lap, UUID> {

    List<Lap> findBySessionIdOrderByLapNumber(UUID sessionId);

    List<Lap> findBySessionIdAndLapNumber(UUID sessionId, Integer lapNumber);

    @Query("SELECT l FROM Lap l WHERE l.sessionId = :sessionId AND l.lapTimeMs = (SELECT MIN(l2.lapTimeMs) FROM Lap l2 WHERE l2.sessionId = :sessionId)")
    Optional<Lap> findFastestLapBySession(@Param("sessionId") UUID sessionId);

    @Query("SELECT l FROM Lap l WHERE l.sessionId = :sessionId ORDER BY l.lapTimeMs ASC")
    List<Lap> findLapsBySessionOrderedByTime(@Param("sessionId") UUID sessionId);

    @Query("SELECT AVG(l.lapTimeMs) FROM Lap l WHERE l.sessionId = :sessionId")
    Optional<Double> findAverageLapTimeBySession(@Param("sessionId") UUID sessionId);

    @Query("SELECT l FROM Lap l WHERE l.sessionId = :sessionId AND l.lapNumber BETWEEN :startLap AND :endLap ORDER BY l.lapNumber")
    List<Lap> findLapsBySessionAndLapRange(@Param("sessionId") UUID sessionId, @Param("startLap") Integer startLap, @Param("endLap") Integer endLap);

    @Query("SELECT COUNT(l) FROM Lap l WHERE l.sessionId = :sessionId")
    Long countLapsBySession(@Param("sessionId") UUID sessionId);

    Optional<Lap> findTopBySessionIdOrderByLapNumberDesc(UUID sessionId);
}