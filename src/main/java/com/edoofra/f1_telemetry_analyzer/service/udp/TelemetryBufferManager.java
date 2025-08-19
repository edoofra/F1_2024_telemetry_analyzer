package com.edoofra.f1_telemetry_analyzer.service.udp;

import com.edoofra.f1_telemetry_analyzer.buffer.TelemetryRingBuffer;
import com.edoofra.f1_telemetry_analyzer.config.TelemetryBufferConfig;
import com.edoofra.f1_telemetry_analyzer.model.BufferStats;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * TelemetryBufferManager is responsible for managing a ring buffer that stores telemetry packets.
 * It provides methods to add, retrieve, and process telemetry packets asynchronously.
 * It also tracks statistics such as total packets received, dropped, and drop rate.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryBufferManager {
    
    private final TelemetryBufferConfig config;
    
    private TelemetryRingBuffer<byte[]> telemetryBuffer;
    private ScheduledExecutorService statsExecutor;
    private volatile long totalPacketsReceived = 0;
    private volatile long totalPacketsDropped = 0;

    /**
     * Initializes the TelemetryBufferManager with a ring buffer of specified capacity.
     * Sets up a scheduled task to log buffer statistics at regular intervals.
     */
    @PostConstruct
    public void initialize() {
        telemetryBuffer = new TelemetryRingBuffer<>(config.getCapacity());
        statsExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "telemetry-buffer-stats");
            t.setDaemon(true);
            return t;
        });
        scheduleStatsLogging();
        log.info("TelemetryBufferManager initialized with capacity: {}", config.getCapacity());
    }
    
    public boolean addTelemetryPacket(byte[] packet) {
        if (packet == null) {
            log.warn("Attempted to add null packet to buffer");
            return false;
        }
        totalPacketsReceived++;
        if (telemetryBuffer.isFull()) {
            totalPacketsDropped++;
            log.debug("Buffer full, oldest packet will be overwritten");
            
            if (shouldLogHighUtilizationWarning()) {
                log.warn("Buffer utilization is high: {:.1f}% - consider increasing capacity or processing rate", 
                    getBufferStats().utilizationPercentage());
            }
        }
        return telemetryBuffer.put(packet);
    }
    
    public byte[] getNextTelemetryPacket() {
        return telemetryBuffer.get();
    }
    
    public byte[] peekNextTelemetryPacket() {
        return telemetryBuffer.peek();
    }
    
    public void processPacketsAsync(Consumer<byte[]> packetProcessor) {
        CompletableFuture.runAsync(() -> {
            byte[] packet;
            while ((packet = getNextTelemetryPacket()) != null) {
                try {
                    packetProcessor.accept(packet);
                } catch (Exception e) {
                    log.error("Error processing telemetry packet", e);
                }
            }
        });
    }
    
    public BufferStats getBufferStats() {
        return new BufferStats(
            telemetryBuffer.size(),
            telemetryBuffer.capacity(),
            totalPacketsReceived,
            totalPacketsDropped,
            calculateDropRate()
        );
    }
    
    public void clearBuffer() {
        telemetryBuffer.clear();
        totalPacketsReceived = 0;
        totalPacketsDropped = 0;
        log.info("Telemetry buffer cleared and counters reset");
    }
    
    public boolean isBufferEmpty() {
        return telemetryBuffer.isEmpty();
    }
    
    public boolean isBufferFull() {
        return telemetryBuffer.isFull();
    }
    
    private double calculateDropRate() {
        if (totalPacketsReceived == 0) {
            return 0.0;
        }
        return (double) totalPacketsDropped / totalPacketsReceived * 100.0;
    }
    
    private void scheduleStatsLogging() {
        statsExecutor.scheduleAtFixedRate(() -> {
            BufferStats stats = getBufferStats();
            log.info("Buffer Stats - Size: {}/{}, Total Received: {}, Dropped: {}, Drop Rate: {:.2f}%",
                stats.currentSize(), stats.capacity(), stats.totalReceived(), 
                stats.totalDropped(), stats.dropRate());
        }, config.getStatsInterval(), config.getStatsInterval(), TimeUnit.SECONDS);
    }
    
    private boolean shouldLogHighUtilizationWarning() {
        return telemetryBuffer.size() >= (telemetryBuffer.capacity() * config.getHighUtilizationThreshold());
    }
}