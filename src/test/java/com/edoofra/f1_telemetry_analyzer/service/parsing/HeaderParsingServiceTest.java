package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

// F1 25 header layout (29 bytes):
// [0-1]  uint16 packetFormat
// [2]    uint8  gameYear
// [3]    uint8  gameMajorVersion
// [4]    uint8  gameMinorVersion
// [5]    uint8  packetVersion
// [6]    uint8  packetId
// [7-14] uint64 sessionUID
// [15-18] float sessionTime
// [19-22] uint32 frameIdentifier
// [23-26] uint32 overallFrameIdentifier
// [27]   uint8  playerCarIndex
// [28]   uint8  secondaryPlayerCarIndex
@ExtendWith(MockitoExtension.class)
@DisplayName("HeaderParsingService Tests")
class HeaderParsingServiceTest {

    private HeaderParsingService headerParsingService;

    @BeforeEach
    void setUp() {
        headerParsingService = new HeaderParsingService();
    }

    @Test
    @DisplayName("Should return null for packet smaller than header size")
    void shouldReturnNullForSmallPacket() {
        byte[] smallPacket = new byte[10];
        assertNull(headerParsingService.parseHeader(smallPacket));
    }

    @Test
    @DisplayName("Should parse valid F1 25 telemetry header")
    void shouldParseValidHeader() {
        byte[] data = createValidHeaderData();

        TelemetryPacketHeader result = headerParsingService.parseHeader(data);

        assertNotNull(result);
        assertEquals(2025, result.packetFormat());
        assertEquals(25, result.gameYear());
        assertEquals(1, result.gameMajorVersion());
        assertEquals(5, result.gameMinorVersion());
        assertEquals(1, result.packetVersion());
        assertEquals(2, result.packetId());
        assertEquals(0x123456789ABCDEF0L, result.sessionUID());
        assertEquals(125.5f, result.sessionTime(), 0.01f);
        assertEquals(1000, result.frameIdentifier());
        assertEquals(500, result.overallFrameIdentifier());
        assertEquals(0, result.playerCarIndex());
        assertEquals(255, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should read packetId from offset 6")
    void shouldHandleDifferentPacketIds() {
        byte[] data = createValidHeaderData();
        data[6] = 3; // Event packet ID

        TelemetryPacketHeader result = headerParsingService.parseHeader(data);

        assertNotNull(result);
        assertEquals(3, result.packetId());
    }

    @Test
    @DisplayName("Should handle maximum values correctly")
    void shouldHandleMaximumValues() {
        byte[] data = new byte[29];

        // packetFormat: 0xFFFF
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xFF;
        // gameYear, gameMajorVersion, gameMinorVersion, packetVersion, packetId
        data[2] = (byte) 0xFF;
        data[3] = (byte) 0xFF;
        data[4] = (byte) 0xFF;
        data[5] = (byte) 0xFF;
        data[6] = (byte) 0xFF;
        // sessionUID: Long.MAX_VALUE
        long maxUid = Long.MAX_VALUE;
        for (int i = 0; i < 8; i++) data[7 + i] = (byte) ((maxUid >>> (i * 8)) & 0xFF);
        // sessionTime: Float.MAX_VALUE
        int maxFloatBits = Float.floatToIntBits(Float.MAX_VALUE);
        for (int i = 0; i < 4; i++) data[15 + i] = (byte) ((maxFloatBits >>> (i * 8)) & 0xFF);
        // frameIdentifier: Integer.MAX_VALUE
        data[19] = (byte) 0xFF; data[20] = (byte) 0xFF; data[21] = (byte) 0xFF; data[22] = (byte) 0x7F;
        // overallFrameIdentifier: Integer.MAX_VALUE
        data[23] = (byte) 0xFF; data[24] = (byte) 0xFF; data[25] = (byte) 0xFF; data[26] = (byte) 0x7F;
        // player indices
        data[27] = (byte) 0xFF;
        data[28] = (byte) 0xFF;

        TelemetryPacketHeader result = headerParsingService.parseHeader(data);

        assertNotNull(result);
        assertEquals(65535, result.packetFormat());
        assertEquals(255, result.gameYear());
        assertEquals(255, result.gameMajorVersion());
        assertEquals(255, result.gameMinorVersion());
        assertEquals(255, result.packetVersion());
        assertEquals(255, result.packetId());
        assertEquals(Long.MAX_VALUE, result.sessionUID());
        assertEquals(Float.MAX_VALUE, result.sessionTime());
        assertEquals(Integer.MAX_VALUE, result.frameIdentifier());
        assertEquals(Integer.MAX_VALUE, result.overallFrameIdentifier());
        assertEquals(255, result.playerCarIndex());
        assertEquals(255, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle zero values correctly")
    void shouldHandleZeroValues() {
        byte[] data = new byte[29];

        TelemetryPacketHeader result = headerParsingService.parseHeader(data);

        assertNotNull(result);
        assertEquals(0, result.packetFormat());
        assertEquals(0, result.gameYear());
        assertEquals(0, result.gameMajorVersion());
        assertEquals(0, result.gameMinorVersion());
        assertEquals(0, result.packetVersion());
        assertEquals(0, result.packetId());
        assertEquals(0L, result.sessionUID());
        assertEquals(0.0f, result.sessionTime());
        assertEquals(0, result.frameIdentifier());
        assertEquals(0, result.overallFrameIdentifier());
        assertEquals(0, result.playerCarIndex());
        assertEquals(0, result.secondaryPlayerCarIndex());
    }

    @Test
    @DisplayName("Should handle exact header size packet")
    void shouldHandleExactHeaderSizePacket() {
        byte[] data = new byte[HeaderParsingService.HEADER_SIZE];
        fillValidHeaderData(data, 0);
        assertNotNull(headerParsingService.parseHeader(data));
    }

    @Test
    @DisplayName("Should handle packet larger than header size")
    void shouldHandleLargerPacket() {
        byte[] data = new byte[100];
        fillValidHeaderData(data, 0);

        TelemetryPacketHeader result = headerParsingService.parseHeader(data);

        assertNotNull(result);
        assertEquals(2025, result.packetFormat());
    }

    private byte[] createValidHeaderData() {
        byte[] data = new byte[29];
        fillValidHeaderData(data, 0);
        return data;
    }

    private void fillValidHeaderData(byte[] data, int o) {
        // packetFormat: 2025 (0x07E9) little-endian
        data[o + 0] = (byte) 0xE9;
        data[o + 1] = (byte) 0x07;
        // gameYear: 25
        data[o + 2] = 25;
        // gameMajorVersion: 1, gameMinorVersion: 5, packetVersion: 1
        data[o + 3] = 1;
        data[o + 4] = 5;
        data[o + 5] = 1;
        // packetId: 2 (LAP_DATA)
        data[o + 6] = 2;
        // sessionUID: 0x123456789ABCDEF0 little-endian
        data[o + 7]  = (byte) 0xF0; data[o + 8]  = (byte) 0xDE;
        data[o + 9]  = (byte) 0xBC; data[o + 10] = (byte) 0x9A;
        data[o + 11] = (byte) 0x78; data[o + 12] = (byte) 0x56;
        data[o + 13] = (byte) 0x34; data[o + 14] = (byte) 0x12;
        // sessionTime: 125.5f
        int timeBits = Float.floatToIntBits(125.5f);
        for (int i = 0; i < 4; i++) data[o + 15 + i] = (byte) ((timeBits >>> (i * 8)) & 0xFF);
        // frameIdentifier: 1000 (0x000003E8)
        data[o + 19] = (byte) 0xE8; data[o + 20] = 0x03; data[o + 21] = 0; data[o + 22] = 0;
        // overallFrameIdentifier: 500 (0x000001F4)
        data[o + 23] = (byte) 0xF4; data[o + 24] = 0x01; data[o + 25] = 0; data[o + 26] = 0;
        // playerCarIndex: 0, secondaryPlayerCarIndex: 255
        data[o + 27] = 0;
        data[o + 28] = (byte) 0xFF;
    }
}
