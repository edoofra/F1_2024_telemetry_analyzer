package com.edoofra.f1_telemetry_analyzer.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TelemetryPacketHeader Tests")
class TelemetryPacketHeaderTest {

    @Test
    @DisplayName("Should create header with builder pattern")
    void shouldCreateHeaderWithBuilder() {
        // Act
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0x123456789ABCDEF0L)
                .withSessionTime(125.5f)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        // Assert
        assertNotNull(header);
        assertEquals(2024, header.packetFormat());
        assertEquals(1, header.gameMajorVersion());
        assertEquals(5, header.gameMinorVersion());
        assertEquals(1, header.packetVersion());
        assertEquals(2, header.packetId());
        assertEquals(0x123456789ABCDEF0L, header.sessionUID());
        assertEquals(125.5f, header.sessionTime());
        assertEquals(1000, header.frameIdentifier());
        assertEquals(0, header.playerCarIndex());
        assertEquals(255, header.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should create header with toBuilder")
    void shouldCreateHeaderWithToBuilder() {
        // Arrange
        TelemetryPacketHeader original = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0x123456789ABCDEF0L)
                .withSessionTime(125.5f)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        // Act - Create modified copy
        TelemetryPacketHeader modified = original.toBuilder()
                .withPacketId(3) // Change packet ID to Event
                .withSessionTime(150.0f) // Update session time
                .build();
        
        // Assert
        assertNotNull(modified);
        assertEquals(2024, modified.packetFormat()); // Unchanged
        assertEquals(1, modified.gameMajorVersion()); // Unchanged
        assertEquals(5, modified.gameMinorVersion()); // Unchanged
        assertEquals(1, modified.packetVersion()); // Unchanged
        assertEquals(3, modified.packetId()); // Changed
        assertEquals(0x123456789ABCDEF0L, modified.sessionUID()); // Unchanged
        assertEquals(150.0f, modified.sessionTime()); // Changed
        assertEquals(1000, modified.frameIdentifier()); // Unchanged
        assertEquals(0, modified.playerCarIndex()); // Unchanged
        assertEquals(255, modified.secondaryPlayerCarIndex()); // Unchanged
        
        // Original should remain unchanged
        assertEquals(2, original.packetId());
        assertEquals(125.5f, original.sessionTime());
    }

    @Test
    @DisplayName("Should handle minimum values")
    void shouldHandleMinimumValues() {
        // Act
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(0)
                .withGameMajorVersion(0)
                .withGameMinorVersion(0)
                .withPacketVersion(0)
                .withPacketId(0)
                .withSessionUID(0L)
                .withSessionTime(0.0f)
                .withFrameIdentifier(0)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(0)
                .build();
        
        // Assert
        assertNotNull(header);
        assertEquals(0, header.packetFormat());
        assertEquals(0, header.gameMajorVersion());
        assertEquals(0, header.gameMinorVersion());
        assertEquals(0, header.packetVersion());
        assertEquals(0, header.packetId());
        assertEquals(0L, header.sessionUID());
        assertEquals(0.0f, header.sessionTime());
        assertEquals(0, header.frameIdentifier());
        assertEquals(0, header.playerCarIndex());
        assertEquals(0, header.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle maximum values")
    void shouldHandleMaximumValues() {
        // Act
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(Integer.MAX_VALUE)
                .withGameMajorVersion(255)
                .withGameMinorVersion(255)
                .withPacketVersion(255)
                .withPacketId(255)
                .withSessionUID(Long.MAX_VALUE)
                .withSessionTime(Float.MAX_VALUE)
                .withFrameIdentifier(Integer.MAX_VALUE)
                .withPlayerCarIndex(255)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        // Assert
        assertNotNull(header);
        assertEquals(Integer.MAX_VALUE, header.packetFormat());
        assertEquals(255, header.gameMajorVersion());
        assertEquals(255, header.gameMinorVersion());
        assertEquals(255, header.packetVersion());
        assertEquals(255, header.packetId());
        assertEquals(Long.MAX_VALUE, header.sessionUID());
        assertEquals(Float.MAX_VALUE, header.sessionTime());
        assertEquals(Integer.MAX_VALUE, header.frameIdentifier());
        assertEquals(255, header.playerCarIndex());
        assertEquals(255, header.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle negative values")
    void shouldHandleNegativeValues() {
        // Act
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(-1)
                .withGameMajorVersion(-1)
                .withGameMinorVersion(-1)
                .withPacketVersion(-1)
                .withPacketId(-1)
                .withSessionUID(-1L)
                .withSessionTime(-123.45f)
                .withFrameIdentifier(-1)
                .withPlayerCarIndex(-1)
                .withSecondaryPlayerCarIndex(-1)
                .build();
        
        // Assert
        assertNotNull(header);
        assertEquals(-1, header.packetFormat());
        assertEquals(-1, header.gameMajorVersion());
        assertEquals(-1, header.gameMinorVersion());
        assertEquals(-1, header.packetVersion());
        assertEquals(-1, header.packetId());
        assertEquals(-1L, header.sessionUID());
        assertEquals(-123.45f, header.sessionTime(), 0.01f);
        assertEquals(-1, header.frameIdentifier());
        assertEquals(-1, header.playerCarIndex());
        assertEquals(-1, header.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle special float values")
    void shouldHandleSpecialFloatValues() {
        // Test NaN
        TelemetryPacketHeader nanHeader = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0L)
                .withSessionTime(Float.NaN)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        assertTrue(Float.isNaN(nanHeader.sessionTime()));
        
        // Test Positive Infinity
        TelemetryPacketHeader infHeader = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0L)
                .withSessionTime(Float.POSITIVE_INFINITY)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        assertEquals(Float.POSITIVE_INFINITY, infHeader.sessionTime());
        
        // Test Negative Infinity
        TelemetryPacketHeader negInfHeader = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0L)
                .withSessionTime(Float.NEGATIVE_INFINITY)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        assertEquals(Float.NEGATIVE_INFINITY, negInfHeader.sessionTime());
    }

    @Test
    @DisplayName("Should maintain equality for same values")
    void shouldMaintainEqualityForSameValues() {
        // Arrange
        TelemetryPacketHeader header1 = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0x123456789ABCDEF0L)
                .withSessionTime(125.5f)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        TelemetryPacketHeader header2 = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0x123456789ABCDEF0L)
                .withSessionTime(125.5f)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        // Assert
        assertEquals(header1, header2);
        assertEquals(header1.hashCode(), header2.hashCode());
    }

    @Test
    @DisplayName("Should have meaningful toString representation")
    void shouldHaveMeaningfulToString() {
        // Arrange
        TelemetryPacketHeader header = TelemetryPacketHeader.builder()
                .withPacketFormat(2024)
                .withGameMajorVersion(1)
                .withGameMinorVersion(5)
                .withPacketVersion(1)
                .withPacketId(2)
                .withSessionUID(0x123456789ABCDEF0L)
                .withSessionTime(125.5f)
                .withFrameIdentifier(1000)
                .withPlayerCarIndex(0)
                .withSecondaryPlayerCarIndex(255)
                .build();
        
        // Act
        String toString = header.toString();
        
        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("2024")); // packet format
        assertTrue(toString.contains("125.5")); // session time
        assertTrue(toString.contains("1000")); // frame identifier
    }
}