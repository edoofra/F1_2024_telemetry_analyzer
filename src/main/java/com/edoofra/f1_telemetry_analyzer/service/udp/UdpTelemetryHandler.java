package com.edoofra.f1_telemetry_analyzer.service.udp;

import com.edoofra.f1_telemetry_analyzer.service.parsing.HeaderParser;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

/**
 * Handles incoming UDP telemetry packets from the F1 2024 game.
 * Parses the raw packet data and logs relevant information.
 * Tracks processing metrics for monitoring and performance analysis with Micrometer.
 */
@Slf4j
@Service
public class UdpTelemetryHandler {

    private final HeaderParser headerParser;
    private final TelemetryBufferManager telemetryBufferManager;
    private final MeterRegistry meterRegistry;
    
    private final Timer packetProcessingTimer;
    private final Counter packetsReceivedCounter;
    private final Counter packetsProcessedCounter;
    private final Counter packetsErrorCounter;
    
    public UdpTelemetryHandler(HeaderParser headerParser,
                               TelemetryBufferManager telemetryBufferManager,
                               MeterRegistry meterRegistry) {
        this.headerParser = headerParser;
        this.telemetryBufferManager = telemetryBufferManager;
        this.meterRegistry = meterRegistry;
        
        this.packetProcessingTimer = Timer.builder("telemetry.packet.processing.time")
            .description("Time taken to process each telemetry packet")
            .register(meterRegistry);
            
        this.packetsReceivedCounter = Counter.builder("telemetry.packets.received")
            .description("Total number of telemetry packets received")
            .register(meterRegistry);
            
        this.packetsProcessedCounter = Counter.builder("telemetry.packets.processed")
            .description("Total number of telemetry packets successfully processed")
            .register(meterRegistry);
            
        this.packetsErrorCounter = Counter.builder("telemetry.packets.errors")
            .description("Total number of telemetry packet processing errors")
            .register(meterRegistry);
    }

    /**
     * Processes incoming UDP telemetry packets with metrics tracking.
     * This method is triggered by the Spring Integration framework
     * when a new message arrives on the configured input channel.
     * 
     * Metrics tracked:
     * - Processing time per packet
     * - Total packets received/processed/errors
     * - Packet size distribution
     *
     * @param message The incoming message containing the UDP packet data.
     */
    @ServiceActivator(inputChannel = "udpInputChannel")
    public void handleTelemetryData(Message<byte[]> message) {
        packetsReceivedCounter.increment();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        byte[] payload = message.getPayload();
        
        log.debug("Received UDP telemetry packet: {} bytes", payload.length);
        
        try {
            boolean success = telemetryBufferManager.addTelemetryPacket(payload);
            
            if (success) {
                packetsProcessedCounter.increment();
                log.trace("Packet successfully added to buffer");
            } else {
                packetsErrorCounter.increment();
                log.warn("Failed to add packet to buffer - packet was null or buffer error");
            }
            
        } catch (Exception e) {
            packetsErrorCounter.increment();
            log.error("Error processing telemetry packet: {}", e.getMessage(), e);
        } finally {
            sample.stop(packetProcessingTimer);
        }
    }
}