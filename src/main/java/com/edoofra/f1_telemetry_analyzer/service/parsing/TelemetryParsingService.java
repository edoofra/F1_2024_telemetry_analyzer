package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.service.parsing.factory.PacketParserFactory;
import com.edoofra.f1_telemetry_analyzer.service.parsing.strategy.PacketParsingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main telemetry parsing service that orchestrates the packet parsing process.
 * 
 * This service follows the strategy pattern:
 * 1. Parse packet header to determine packet type
 * 2. Use factory to get appropriate parsing strategy
 * 3. Delegate parsing to the specific strategy
 * 4. Handle session management and persistence
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryParsingService {
    
    private final PacketParserFactory packetParserFactory;
    
    /**
     * Main entry point for parsing telemetry packets.
     * 
     * @param packetData Raw packet data from F1 game
     * @return true if packet was successfully parsed, false otherwise
     */
    public boolean parsePacket(byte[] packetData) {
        if (packetData == null || packetData.length == 0) {
            log.warn("Received null or empty packet data");
            return false;
        }
        
        try {
            // Step 1: Parse packet header to determine packet type
            TelemetryPacketHeader header = HeaderParser.parseHeader(packetData);
            
            if (header == null) {
                log.warn("Failed to parse packet header from {} bytes", packetData.length);
                return false;
            }

            log.debug("Parsed packet header: type={}, sessionId={}, frameId={}",
                header.packetId(), header.sessionUID(), header.frameIdentifier());

            // Step 2: Get appropriate parsing strategy based on packet type
            PacketParsingStrategy strategy = packetParserFactory.getParsingStrategy(header.packetId());

            // Step 3: Parse the packet using the specific strategy
            boolean success = strategy.parsePacket(header, packetData);
            
            if (success) {
                log.trace("Successfully parsed packet type {} for session {}", 
                    header.packetId(), header.sessionUID());
            } else {
                log.warn("Failed to parse packet type {} for session {}", 
                    header.packetId(), header.sessionUID());
            }
            
            return success;
            
        } catch (Exception e) {
            log.error("Error parsing telemetry packet of {} bytes", packetData.length, e);
            return false;
        }
    }
}