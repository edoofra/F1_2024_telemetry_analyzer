package com.edoofra.f1_telemetry_analyzer.service.udp;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.service.parsing.HeaderParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Handles incoming UDP telemetry packets from the F1 2024 game.
 * Parses the raw packet data and logs relevant information.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UdpTelemetryHandler {

    private final HeaderParsingService headerParsingService;

    /**
     * Processes incoming UDP telemetry packets.
     * This method is triggered by the Spring Integration framework
     * when a new message arrives on the configured input channel.
     *
     * @param message The incoming message containing the UDP packet data.
     */
    @ServiceActivator(inputChannel = "udpInputChannel")
    public void handleTelemetryData(Message<byte[]> message) {
        byte[] payload = message.getPayload();
        log.debug("Received UDP telemetry packet: {} bytes", payload.length);
        
        try {
            parseTelemetryPacket(payload);
        } catch (Exception e) {
            log.error("Error processing telemetry packet", e);
        }
    }

    private void parseTelemetryPacket(byte[] data) {
        TelemetryPacketHeader header = headerParsingService.parseHeader(data);
        if (header == null) {
            return;
        }

        log.info("F1 Telemetry - Packet ID: {}, Session: {}, Time: {:.3f}s, Frame: {}", 
                 header.packetId(), header.sessionUID(), header.sessionTime(), header.frameIdentifier());

        // TODO: Parse specific packet types based on packetId
        // 0 = Motion, 1 = Session, 2 = Lap Data, 3 = Event, etc.
    }
}