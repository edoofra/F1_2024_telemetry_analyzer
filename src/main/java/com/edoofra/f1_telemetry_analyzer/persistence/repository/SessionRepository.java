package com.edoofra.f1_telemetry_analyzer.persistence.repository;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {

    List<Session> findByType(String type);

    List<Session> findByTrackName(String trackName);

    List<Session> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Session s WHERE s.closedAt IS NULL")
    List<Session> findActiveSessions();

    @Query("SELECT s FROM Session s WHERE s.type = :type AND s.createdAt >= :since ORDER BY s.createdAt DESC")
    List<Session> findRecentSessionsByType(@Param("type") String type, @Param("since") LocalDateTime since);
}