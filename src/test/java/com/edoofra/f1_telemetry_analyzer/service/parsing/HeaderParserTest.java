package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HeaderParser Tests")
class HeaderParserTest {

    private HeaderParser headerParser;

    @BeforeEach
    void setUp() {
        headerParser = new HeaderParser();
    }

    @Test
    @DisplayName("Should return null for packet smaller than header size")
    void shouldReturnNullForSmallPacket() {
        // Arrange
        byte[] smallPacket = new byte[10]; // Less than 24 bytes
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(smallPacket);
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should parse valid F1 telemetry header")
    void shouldParseValidHeader() {
        // Arrange: Create a valid 24-byte header
        byte[] headerData = createValidHeaderData();
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(headerData);
        
        // Assert
        assertNotNull(result);
        assertEquals(2024, result.packetFormat());
        assertEquals(1, result.gameMajorVersion());
        assertEquals(5, result.gameMinorVersion());
        assertEquals(1, result.packetVersion());
        assertEquals(2, result.packetId()); // Lap data packet
        assertEquals(0x123456789ABCDEF0L, result.sessionUID());
        assertEquals(125.5f, result.sessionTime(), 0.01f);
        assertEquals(1000, result.frameIdentifier());
        assertEquals(0, result.playerCarIndex());
        assertEquals(255, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle different packet IDs")
    void shouldHandleDifferentPacketIds() {
        // Arrange
        byte[] headerData = createValidHeaderData();
        headerData[5] = 3; // Event packet ID
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(headerData);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.packetId());
    }

    @Test
    @DisplayName("Should handle maximum values correctly")
    void shouldHandleMaximumValues() {
        // Arrange: Header with maximum values
        byte[] headerData = new byte[24];
        
        // Packet format: 0xFFFF (little-endian)
        headerData[0] = (byte) 0xFF;
        headerData[1] = (byte) 0xFF;
        
        // Game versions: 255
        headerData[2] = (byte) 0xFF; // major
        headerData[3] = (byte) 0xFF; // minor
        headerData[4] = (byte) 0xFF; // packet version
        headerData[5] = (byte) 0xFF; // packet ID
        
        // Session UID: maximum long value
        long maxSessionUID = Long.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            headerData[6 + i] = (byte) ((maxSessionUID >>> (i * 8)) & 0xFF);
        }
        
        // Session time: maximum float value
        int maxFloatBits = Float.floatToIntBits(Float.MAX_VALUE);
        for (int i = 0; i < 4; i++) {
            headerData[14 + i] = (byte) ((maxFloatBits >>> (i * 8)) & 0xFF);
        }
        
        // Frame identifier: maximum int
        headerData[18] = (byte) 0xFF;
        headerData[19] = (byte) 0xFF;
        headerData[20] = (byte) 0xFF;
        headerData[21] = (byte) 0x7F; // Integer.MAX_VALUE
        
        // Player indices
        headerData[22] = (byte) 0xFF;
        headerData[23] = (byte) 0xFF;
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(headerData);
        
        // Assert
        assertNotNull(result);
        assertEquals(65535, result.packetFormat());
        assertEquals(255, result.gameMajorVersion());
        assertEquals(255, result.gameMinorVersion());
        assertEquals(255, result.packetVersion());
        assertEquals(255, result.packetId());
        assertEquals(Long.MAX_VALUE, result.sessionUID());
        assertEquals(Float.MAX_VALUE, result.sessionTime());
        assertEquals(Integer.MAX_VALUE, result.frameIdentifier());
        assertEquals(255, result.playerCarIndex());
        assertEquals(255, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle zero values correctly")
    void shouldHandleZeroValues() {
        // Arrange: Header with all zeros
        byte[] headerData = new byte[24]; // All bytes initialized to 0
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(headerData);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.packetFormat());
        assertEquals(0, result.gameMajorVersion());
        assertEquals(0, result.gameMinorVersion());
        assertEquals(0, result.packetVersion());
        assertEquals(0, result.packetId());
        assertEquals(0L, result.sessionUID());
        assertEquals(0.0f, result.sessionTime());
        assertEquals(0, result.frameIdentifier());
        assertEquals(0, result.playerCarIndex());
        assertEquals(0, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle exact header size packet")
    void shouldHandleExactHeaderSizePacket() {
        // Arrange
        byte[] exactSizePacket = new byte[HeaderParser.HEADER_SIZE];
        fillValidHeaderData(exactSizePacket, 0);
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(exactSizePacket);
        
        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle packet larger than header size")
    void shouldHandleLargerPacket() {
        // Arrange
        byte[] largerPacket = new byte[100]; // Larger than header
        fillValidHeaderData(largerPacket, 0);
        
        // Act
        TelemetryPacketHeader result = headerParser.parseHeader(largerPacket);
        
        // Assert
        assertNotNull(result);
        assertEquals(2024, result.packetFormat());
    }

    /**
     * Creates a valid 24-byte F1 telemetry header for testing.
     */
    private byte[] createValidHeaderData() {
        byte[] data = new byte[24];
        fillValidHeaderData(data, 0);
        return data;
    }

    /**
     * Fills byte array with valid F1 telemetry header data at specified offset.
     */
    private void fillValidHeaderData(byte[] data, int offset) {
        // Packet format: 2024 (0x07E8) in little-endian
        data[offset + 0] = (byte) 0xE8;
        data[offset + 1] = (byte) 0x07;
        
        // Game version: 1.5
        data[offset + 2] = 1; // major
        data[offset + 3] = 5; // minor
        data[offset + 4] = 1; // packet version
        data[offset + 5] = 2; // packet ID (lap data)
        
        // Session UID: 0x123456789ABCDEF0 in little-endian
        data[offset + 6] = (byte) 0xF0;
        data[offset + 7] = (byte) 0xDE;
        data[offset + 8] = (byte) 0xBC;
        data[offset + 9] = (byte) 0x9A;
        data[offset + 10] = (byte) 0x78;
        data[offset + 11] = (byte) 0x56;
        data[offset + 12] = (byte) 0x34;
        data[offset + 13] = (byte) 0x12;
        
        // Session time: 125.5f in little-endian IEEE 754
        int timeBits = Float.floatToIntBits(125.5f);
        data[offset + 14] = (byte) (timeBits & 0xFF);
        data[offset + 15] = (byte) ((timeBits >>> 8) & 0xFF);
        data[offset + 16] = (byte) ((timeBits >>> 16) & 0xFF);
        data[offset + 17] = (byte) ((timeBits >>> 24) & 0xFF);
        
        // Frame identifier: 1000 (0x03E8) in little-endian
        data[offset + 18] = (byte) 0xE8;
        data[offset + 19] = (byte) 0x03;
        data[offset + 20] = (byte) 0x00;
        data[offset + 21] = (byte) 0x00;
        
        // Player car index: 0
        data[offset + 22] = 0;
        
        // Secondary player car index: 255 (no secondary player)
        data[offset + 23] = (byte) 0xFF;
    }
}