package com.edoofra.f1_telemetry_analyzer.service.session;

import com.edoofra.f1_telemetry_analyzer.model.packets.SessionPacketData;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import com.edoofra.f1_telemetry_analyzer.persistence.repository.SessionRepository;
import com.edoofra.f1_telemetry_analyzer.util.SessionTypeMapper;
import com.edoofra.f1_telemetry_analyzer.util.TrackNames;
import com.edoofra.f1_telemetry_analyzer.util.WeatherNames;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementService {

    private final SessionRepository sessionRepository;

    // AtomicReferences are safe here because TelemetryProcessingService uses a single processing thread
    // while the scheduler runs on a separate thread.
    private final AtomicReference<String> activeSessionUid = new AtomicReference<>();
    private final AtomicReference<SessionPacketData> lastPacket = new AtomicReference<>();

    @Transactional
    public void process(SessionPacketData packet) {
        String incomingUid = String.valueOf(packet.header().sessionUID());
        String currentUid = activeSessionUid.get();

        if (!incomingUid.equals(currentUid)) {
            if (currentUid != null) {
                closeSession(currentUid);
            }
            openSession(packet, incomingUid);
            activeSessionUid.set(incomingUid);
        }

        lastPacket.set(packet);
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    @Transactional
    public void snapshotActiveSession() {
        String uid = activeSessionUid.get();
        SessionPacketData packet = lastPacket.get();
        if (uid == null || packet == null) {
            return;
        }

        sessionRepository.findByGameSessionId(uid).ifPresent(session -> {
            applyMutableFields(session, packet);
            sessionRepository.save(session);
            log.info("Periodic snapshot saved for session {} — weather: {}, SC status: {}, red flags: {}",
                    uid, session.getWeather(), session.getSafetyCarStatus(), session.getNumRedFlagPeriods());
        });
    }

    private void openSession(SessionPacketData packet, String sessionUid) {
        if (sessionRepository.findByGameSessionId(sessionUid).isPresent()) {
            log.info("Session {} already exists in DB, resuming without re-creating", sessionUid);
            return;
        }

        String gameVersion = "%d.%d.%d".formatted(
                packet.header().gameYear(),
                packet.header().gameMajorVersion(),
                packet.header().gameMinorVersion()
        );

        Session session = Session.builder()
                .withGameSessionId(sessionUid)
                .withType(SessionTypeMapper.fromId(packet.sessionType()))
                .withTrackName(TrackNames.fromId(packet.trackId()))
                .withGameVersion(gameVersion)
                .build();

        applyMutableFields(session, packet);
        sessionRepository.save(session);
        log.info("Opened session {} — track: {}, type: {}, weather: {}",
                sessionUid, session.getTrackName(), session.getType(), session.getWeather());
    }

    private void closeSession(String sessionUid) {
        sessionRepository.findByGameSessionId(sessionUid).ifPresent(session -> {
            // Final snapshot of mutable state before closing
            SessionPacketData packet = lastPacket.get();
            if (packet != null) {
                applyMutableFields(session, packet);
            }
            session.setClosedAt(LocalDateTime.now());
            sessionRepository.save(session);
            log.info("Closed session {} ({})", sessionUid, session.getTrackName());
        });
    }

    private void applyMutableFields(Session session, SessionPacketData packet) {
        session.setWeather(WeatherNames.fromId(packet.weather()));
        session.setTrackTemperature(packet.trackTemperature());
        session.setAirTemperature(packet.airTemperature());
        session.setSafetyCarStatus(packet.safetyCarStatus());
        session.setNumSafetyCarPeriods(packet.numSafetyCarPeriods());
        session.setNumVirtualSafetyCarPeriods(packet.numVirtualSafetyCarPeriods());
        session.setNumRedFlagPeriods(packet.numRedFlagPeriods());
    }

    @PreDestroy
    @Transactional
    public void onShutdown() {
        String uid = activeSessionUid.get();
        if (uid != null) {
            log.info("Application shutting down — closing active session {}", uid);
            closeSession(uid);
        }
    }
}
