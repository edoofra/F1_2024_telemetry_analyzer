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
public final class HeaderParser {

    public static final int HEADER_SIZE = 24;

    /**
     * Parses the header from a raw telemetry packet.
     *
     * @param data Raw packet data
     * @return Parsed header object, or null if packet is too small
     */
    public static TelemetryPacketHeader parseHeader(byte[] data) {
        if (data.length < HEADER_SIZE) {
            log.warn("Packet too small for header parsing: {} bytes (minimum {})", data.length, HEADER_SIZE);
            return null;
        }

        try {
            return TelemetryPacketHeader.builder()
                    .withPacketFormat(BinaryDataUtils.getUnsignedShort(data, 0))
                    .withGameMajorVersion(data[2] & 0xFF)
                    .withGameMinorVersion(data[3] & 0xFF)
                    .withPacketVersion(data[4] & 0xFF)
                    .withPacketId(data[5] & 0xFF)
                    .withSessionUID(BinaryDataUtils.getLong(data, 6))
                    .withSessionTime(BinaryDataUtils.getFloat(data, 14))
                    .withFrameIdentifier(BinaryDataUtils.getInt(data, 18))
                    .withPlayerCarIndex(data[22] & 0xFF)
                    .withSecondaryPlayerCarIndex(data[23] & 0xFF)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing packet header", e);
            return null;
        }
    }
}