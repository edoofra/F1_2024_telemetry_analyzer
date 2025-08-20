package com.edoofra.f1_telemetry_analyzer.service.session;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum;
import com.edoofra.f1_telemetry_analyzer.persistence.repository.LapRepository;
import com.edoofra.f1_telemetry_analyzer.persistence.repository.SessionRepository;
import com.edoofra.f1_telemetry_analyzer.service.lap.LapDataMergeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing F1 telemetry sessions.
 * <p>
 * Handles session creation, loading, and persistence of session-related data.
 * Uses caching to avoid database lookups for active sessions.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {
    private final ConcurrentHashMap<String, Session> activeSessionCache = new ConcurrentHashMap<>();

    private final SessionRepository sessionRepository;
    private final LapRepository lapRepository;
    private final LapDataMergeService lapDataMergeService;

    /**
     * Get or create a session based on the telemetry packet header.
     *
     * @param header Telemetry packet header containing session information
     * @return Session entity (existing or newly created)
     */
    @Transactional
    public Session getOrCreateSession(TelemetryPacketHeader header) {
        String gameSessionId = getGameSessionId(header);
        return Optional.ofNullable(activeSessionCache.get(gameSessionId))
                .or(() -> sessionRepository.findByGameSessionId(gameSessionId))
                .map(existingSession -> {
                    activeSessionCache.put(gameSessionId, existingSession);
                    return existingSession;
                })
                .orElseGet(() -> createAndCacheNewSession(header));
    }

    private static String getGameSessionId(TelemetryPacketHeader header) {
        return Optional.ofNullable(header.sessionUID())
                .map(String::valueOf)
                .orElse(null);
    }

    /**
     * Create a new session from telemetry header information.
     */
    private Session createAndCacheNewSession(TelemetryPacketHeader header) {
        try {

            Session session = Session.builder()
                    .withGameSessionId(Optional.ofNullable(header.sessionUID())
                            .map(String::valueOf)
                            .orElse(null))
                    .withType(determineSessionType(header))
                    .withTrackName(determineTrackName(header))
                    .withWeather("Unknown")
                    .withCreatedAt(LocalDateTime.now())
                    .build();
            return Optional.of(sessionRepository.save(session))
                    .map(savedSession -> activeSessionCache.put(savedSession.getGameSessionId(), savedSession))
                    .orElse(null);

        } catch (Exception e) {
            log.error("Failed to create new session for gameSessionId: {}", header.sessionUID(), e);
            return null;
        }
    }

    /**
     * Close a session (mark it as completed).
     */
    @Transactional
    public void closeSession(String gameSessionId) {
        Optional.ofNullable(activeSessionCache.get(gameSessionId))
                .or(() -> sessionRepository.findByGameSessionId(gameSessionId))
                .filter(session -> session.getClosedAt() == null)
                .ifPresent(session -> {
                    session.setClosedAt(LocalDateTime.now());
                    sessionRepository.save(session);
                    activeSessionCache.remove(gameSessionId);
                    log.info("Closed session: {} at {}", gameSessionId, session.getClosedAt());
                });
    }

    /**
     * Clear the session cache (useful for testing or administrative purposes).
     */
    public void clearSessionCache() {
        activeSessionCache.clear();
        log.info("Cleared session cache");
    }

    /**
     * Determine session type from header information.
     * For now, defaults to UNKNOWN - will be enhanced when session packets are parsed.
     */
    private SessionTypeEnum determineSessionType(TelemetryPacketHeader header) {
        // TODO: Implement proper session type detection from session packet
        // For now, return UNKNOWN until we implement session packet parsing
        return SessionTypeEnum.UNDEFINED;
    }

    /**
     * Determine track name from header information.
     * For now, returns generic name - will be enhanced when session packets are parsed.
     */
    private String determineTrackName(TelemetryPacketHeader header) {
        // TODO: Implement proper track name detection from session packet
        // For now, return a placeholder until we implement session packet parsing
        return "Unknown Track";
    }
}