package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.util.BinaryDataUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for parsing F1 2024 telemetry packet headers.
 * Extracts header information from raw UDP packet data.
 */
@Slf4j
@Service
public class HeaderParsingService {

    // F1 25 header layout (29 bytes):
    // uint16 packetFormat, uint8 gameYear, uint8 gameMajorVersion, uint8 gameMinorVersion,
    // uint8 packetVersion, uint8 packetId, uint64 sessionUID, float sessionTime,
    // uint32 frameIdentifier, uint32 overallFrameIdentifier, uint8 playerCarIndex, uint8 secondaryPlayerCarIndex
    public static final int HEADER_SIZE = 29;

    /**
     * Parses the header from a raw telemetry packet.
     *
     * @param data Raw packet data
     * @return Parsed header object, or null if packet is too small
     */
    public TelemetryPacketHeader parseHeader(byte[] data) {
        if (data.length < HEADER_SIZE) {
            log.warn("Packet too small for header parsing: {} bytes (minimum {})", data.length, HEADER_SIZE);
            return null;
        }

        try {
            return TelemetryPacketHeader.builder()
                    .withPacketFormat(BinaryDataUtils.getUnsignedShort(data, 0))
                    .withGameYear(data[2] & 0xFF)
                    .withGameMajorVersion(data[3] & 0xFF)
                    .withGameMinorVersion(data[4] & 0xFF)
                    .withPacketVersion(data[5] & 0xFF)
                    .withPacketId(data[6] & 0xFF)
                    .withSessionUID(BinaryDataUtils.getLong(data, 7))
                    .withSessionTime(BinaryDataUtils.getFloat(data, 15))
                    .withFrameIdentifier(BinaryDataUtils.getInt(data, 19))
                    .withOverallFrameIdentifier(BinaryDataUtils.getInt(data, 23))
                    .withPlayerCarIndex(data[27] & 0xFF)
                    .withSecondaryPlayerCarIndex(data[28] & 0xFF)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing packet header", e);
            return null;
        }
    }
}