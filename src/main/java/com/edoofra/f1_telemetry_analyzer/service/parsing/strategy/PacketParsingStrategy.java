package com.edoofra.f1_telemetry_analyzer.service.parsing.strategy;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;

/**
 * Strategy interface for parsing different types of F1 telemetry packets.
 * 
 * Each packet type (lap data, session data, car telemetry, etc.) will have
 * its own implementation of this strategy.
 */
public interface PacketParsingStrategy {
    
    /**
     * Parse the specific packet type and handle the data appropriately.
     * 
     * @param header Parsed packet header containing metadata
     * @param packetData Complete raw packet data
     * @return true if packet was successfully parsed and processed, false otherwise
     */
    boolean parsePacket(TelemetryPacketHeader header, byte[] packetData);
    
    /**
     * Get the packet ID that this strategy handles.
     * 
     * @return packet ID as defined in F1 telemetry specification
     */
    int getSupportedPacketId();
    
    /**
     * Get a human-readable name for this parser strategy.
     * 
     * @return strategy name for logging and monitoring
     */
    String getStrategyName();
}