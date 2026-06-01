package com.edoofra.f1_telemetry_analyzer.service.session;

import com.edoofra.f1_telemetry_analyzer.annotation.IntegrationTest;
import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.model.packets.SessionPacketData;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum;
import com.edoofra.f1_telemetry_analyzer.persistence.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@DisplayName("SessionManagementService Integration Tests")
class SessionManagementServiceIntegrationTest {

    @Autowired
    private SessionManagementService sessionManagementService;

    @Autowired
    private SessionRepository sessionRepository;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
    }

    @Nested
    @DisplayName("Session lifecycle")
    class SessionLifecycle {

        @Test
        @DisplayName("Should create a new session when first packet arrives")
        void shouldCreateNewSession() {
            sessionManagementService.process(packet("uid-monaco-race", 5, 15, 0));

            Session session = sessionRepository.findByGameSessionId("uid-monaco-race").orElseThrow();
            assertEquals("Monaco", session.getTrackName());
            assertEquals(SessionTypeEnum.RACE, session.getType());
            assertEquals("Clear", session.getWeather());
            assertEquals("25.1.0", session.getGameVersion());
            assertNull(session.getClosedAt());
            assertNotNull(session.getCreatedAt());
        }

        @Test
        @DisplayName("Should not create a duplicate session on repeated packets with the same UID")
        void shouldNotDuplicateSessionOnRepeatedPackets() {
            sessionManagementService.process(packet("uid-spa-race", 10, 15, 0));
            sessionManagementService.process(packet("uid-spa-race", 10, 15, 0));
            sessionManagementService.process(packet("uid-spa-race", 10, 15, 0));

            assertEquals(1, sessionRepository.count());
        }

        @Test
        @DisplayName("Should close the previous session when a new UID arrives")
        void shouldClosePreviousSessionOnNewUid() {
            sessionManagementService.process(packet("uid-session-a", 5, 15, 0));
            sessionManagementService.process(packet("uid-session-b", 11, 5, 0));

            Session closed = sessionRepository.findByGameSessionId("uid-session-a").orElseThrow();
            assertNotNull(closed.getClosedAt(), "Previous session should be closed");

            Session open = sessionRepository.findByGameSessionId("uid-session-b").orElseThrow();
            assertNull(open.getClosedAt(), "New session should still be open");
        }

        @Test
        @DisplayName("Should not create a duplicate when restarting mid-session (recovery)")
        void shouldRecoverGracefullyOnRestart() {
            // Simulate a session row already persisted before the app restarted
            Session existing = Session.builder()
                    .withGameSessionId("uid-existing")
                    .withType(SessionTypeEnum.RACE)
                    .withTrackName("Monaco")
                    .withWeather("Clear")
                    .build();
            sessionRepository.save(existing);

            // App restarts and receives the same sessionUID again
            sessionManagementService.process(packet("uid-existing", 5, 15, 0));

            assertEquals(1, sessionRepository.count(), "Should not insert a duplicate row");
        }

        @Test
        @DisplayName("Should correctly map all session types")
        void shouldMapSessionTypesCorrectly() {
            sessionManagementService.process(packet("uid-p1",  5, 1, 0));
            sessionManagementService.process(packet("uid-q1",  5, 5, 0));
            sessionManagementService.process(packet("uid-race",5, 15, 0));
            sessionManagementService.process(packet("uid-tt",  5, 18, 0));

            assertEquals(SessionTypeEnum.PRACTICE_1,   sessionRepository.findByGameSessionId("uid-p1").orElseThrow().getType());
            assertEquals(SessionTypeEnum.QUALIFYING_1,  sessionRepository.findByGameSessionId("uid-q1").orElseThrow().getType());
            assertEquals(SessionTypeEnum.RACE,          sessionRepository.findByGameSessionId("uid-race").orElseThrow().getType());
            assertEquals(SessionTypeEnum.TIME_TRIAL,    sessionRepository.findByGameSessionId("uid-tt").orElseThrow().getType());
        }
    }

    @Nested
    @DisplayName("Mutable fields")
    class MutableFields {

        @Test
        @DisplayName("Should persist initial mutable fields when session opens")
        void shouldPersistInitialMutableFields() {
            sessionManagementService.process(
                    packet("uid-mutable", 11, 15, 3, 28, 22, 1, 0, 0));

            Session session = sessionRepository.findByGameSessionId("uid-mutable").orElseThrow();
            assertEquals("Light Rain", session.getWeather());
            assertEquals(28, session.getTrackTemperature());
            assertEquals(22, session.getAirTemperature());
            assertEquals(1, session.getSafetyCarStatus());
            assertEquals(0, session.getNumSafetyCarPeriods());
            assertEquals(0, session.getNumRedFlagPeriods());
        }

        @Test
        @DisplayName("Should update mutable fields on periodic snapshot")
        void shouldUpdateMutableFieldsOnSnapshot() {
            // Open session with clear weather
            sessionManagementService.process(packet("uid-snapshot", 10, 15, 0, 30, 26, 0, 0, 0));

            // Weather deteriorates and a safety car is deployed
            sessionManagementService.process(packet("uid-snapshot", 10, 15, 4, 21, 18, 1, 1, 0));

            // Trigger the periodic snapshot manually
            sessionManagementService.snapshotActiveSession();

            Session session = sessionRepository.findByGameSessionId("uid-snapshot").orElseThrow();
            assertEquals("Heavy Rain", session.getWeather());
            assertEquals(21, session.getTrackTemperature());
            assertEquals(1, session.getSafetyCarStatus());
            assertEquals(1, session.getNumSafetyCarPeriods());
        }

        @Test
        @DisplayName("Should capture final mutable state when session closes")
        void shouldCaptureFinalStateOnClose() {
            // Open session with benign conditions
            sessionManagementService.process(packet("uid-close-test", 5, 15, 0, 32, 28, 0, 0, 0));
            // Conditions worsen before close
            sessionManagementService.process(packet("uid-close-test", 5, 15, 5, 16, 14, 1, 2, 1));

            // A new session arrives, triggering the close of uid-close-test
            sessionManagementService.process(packet("uid-next", 11, 15, 0, 30, 25, 0, 0, 0));

            Session closed = sessionRepository.findByGameSessionId("uid-close-test").orElseThrow();
            assertEquals("Storm", closed.getWeather());
            assertEquals(2, closed.getNumSafetyCarPeriods());
            assertEquals(1, closed.getNumRedFlagPeriods());
            assertNotNull(closed.getClosedAt());
        }

        @Test
        @DisplayName("Snapshot should be a no-op when no session is active")
        void snapshotShouldBeNoOpWhenNoActiveSession() {
            assertDoesNotThrow(() -> sessionManagementService.snapshotActiveSession());
            assertEquals(0, sessionRepository.count());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private SessionPacketData packet(String uid, int trackId, int sessionType, int weather) {
        return packet(uid, trackId, sessionType, weather, 30, 25, 0, 0, 0);
    }

    private SessionPacketData packet(String uid, int trackId, int sessionType, int weather,
                                     int trackTemp, int airTemp,
                                     int safetyCarStatus, int numSafetyCarPeriods, int numRedFlagPeriods) {
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(2025)
                .withGameYear(25)
                .withGameMajorVersion(1)
                .withGameMinorVersion(0)
                .withPacketVersion(1)
                .withPacketId(1)
                .withSessionUID((long) uid.hashCode())
                .withSessionTime(0f)
                .withFrameIdentifier(0)
                .withOverallFrameIdentifier(0)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();

        return new SessionPacketData(
                header,
                weather, trackTemp, airTemp,
                50, 5412,
                sessionType, trackId, 0,
                3600, 7200, 80,
                false, false, 255,
                0, List.of(),
                safetyCarStatus, false,
                0, List.of(),
                0, 90,
                1L, 1L, 1L,
                10, 15, 5,
                0, 0, 720L, 4,
                numSafetyCarPeriods, 0, numRedFlagPeriods,
                1500f, 2800f
        );
    }
}
