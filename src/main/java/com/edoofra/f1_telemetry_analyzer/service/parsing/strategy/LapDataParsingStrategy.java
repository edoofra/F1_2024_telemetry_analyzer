package com.edoofra.f1_telemetry_analyzer.service.parsing.strategy;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import com.edoofra.f1_telemetry_analyzer.service.lap.LapService;
import com.edoofra.f1_telemetry_analyzer.service.parsing.parser.LapDataParser;
import com.edoofra.f1_telemetry_analyzer.service.session.SessionService;
import com.edoofra.f1_telemetry_analyzer.util.BinaryDataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Parsing strategy for F1 lap data packets (packet ID 2).
 * 
 * This strategy parses lap data including lap times, sector times, and positions,
 * then persists the data to the database associated with the current session.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LapDataParsingStrategy implements PacketParsingStrategy {
    
    private static final int LAP_DATA_PACKET_ID = 2;
    private static final String STRATEGY_NAME = "LapDataParser";
    private static final int LAP_DATA_SIZE = 53; // Size of each lap data entry
    private static final int MAX_CARS = 22; // Maximum number of cars in F1
    
    private final SessionService sessionService;
    private final LapService lapService;
    
    @Override
    public boolean parsePacket(TelemetryPacketHeader header, byte[] packetData) {
        try {
            log.debug("Parsing lap data packet for session {}, frame {}", 
                header.sessionUID(), header.frameIdentifier());
            
            // Load or create session
            Session session = sessionService.getOrCreateSession(header);
            if (session == null) {
                log.error("Failed to get or create session for lap data");
                return false;
            }

            Integer playerCarIndex = getPlayerCarIndex(header);
            if (playerCarIndex == null) return false;

            Integer playerDataOffset = getOffset(packetData, playerCarIndex);
            if (playerDataOffset == null) return false;

            // Parse lap data fields
            Lap lapData = LapDataParser.parseLapDataFromBytes(packetData, playerDataOffset, session);
            
            if (lapData != null) {
                lapService.saveLapData(lapData);
                return true;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error parsing lap data packet", e);
            return false;
        }
    }

    private static Integer getPlayerCarIndex(TelemetryPacketHeader header) {
        // Parse lap data for player car (index 0 in most cases)
        // For now, we'll focus on the player's car data
        int playerCarIndex = header.playerCarIndex();
        if (playerCarIndex < 0 || playerCarIndex >= MAX_CARS) {
            log.warn("Invalid player car index: {}", playerCarIndex);
            return null;
        }
        return playerCarIndex;
    }

    private static Integer getOffset(byte[] packetData, int playerCarIndex) {
        // Calculate offset for player car data
        int headerSize = 24; // F1 2024 header size
        int playerDataOffset = headerSize + (playerCarIndex * LAP_DATA_SIZE);

        if (playerDataOffset + LAP_DATA_SIZE > packetData.length) {
            log.warn("Packet too small for lap data: expected {}, got {}",
                playerDataOffset + LAP_DATA_SIZE, packetData.length);
            return null;
        }
        return playerDataOffset;
    }
    
    @Override
    public int getSupportedPacketId() {
        return LAP_DATA_PACKET_ID;
    }
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
}