package com.edoofra.f1_telemetry_analyzer.service.processing;

import com.edoofra.f1_telemetry_analyzer.service.udp.TelemetryBufferManager;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Service responsible for continuously processing telemetry packets from the ring buffer.
 * Runs a background thread that polls the buffer and processes packets as they arrive.
 * 
 * This service provides the connection between the UDP ingestion layer and the
 * telemetry processing/session management layers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryProcessingService {

    private final TelemetryBufferManager bufferManager;
    
    @Value("${f1.telemetry.processing.thread-count:1}")
    private int processingThreadCount;
    
    @Value("${f1.telemetry.processing.poll-interval-ms:10}")
    private int pollIntervalMs;
    
    @Value("${f1.telemetry.processing.enabled:true}")
    private boolean processingEnabled;
    
    private ExecutorService processingExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    @PostConstruct
    public void initialize() {
        if (!processingEnabled) {
            log.info("Telemetry processing is disabled");
            return;
        }
        
        processingExecutor = Executors.newFixedThreadPool(processingThreadCount, r -> {
            Thread t = new Thread(r, "telemetry-processor");
            t.setDaemon(true);
            return t;
        });
        
        startProcessing();
        log.info("TelemetryProcessingService initialized with {} threads, poll interval: {}ms", 
            processingThreadCount, pollIntervalMs);
    }
    
    /**
     * Starts the background processing threads.
     */
    public void startProcessing() {
        if (!processingEnabled || running.get()) {
            return;
        }
        
        running.set(true);
        
        for (int i = 0; i < processingThreadCount; i++) {
            final int threadId = i;
            processingExecutor.submit(() -> processingLoop(threadId));
        }
        
        log.info("Started {} telemetry processing threads", processingThreadCount);
    }
    
    /**
     * Stops the background processing threads.
     */
    public void stopProcessing() {
        running.set(false);
        log.info("Stopping telemetry processing threads");
    }
    
    /**
     * Main processing loop that continuously polls the buffer for new packets.
     */
    private void processingLoop(int threadId) {
        log.debug("Processing thread {} started", threadId);
        
        while (running.get()) {
            try {
                byte[] packet = bufferManager.getNextTelemetryPacket();
                
                if (packet != null) {
                    processPacket(packet, threadId);
                } else {
                    // No packets available, sleep briefly to avoid busy waiting
                    Thread.sleep(pollIntervalMs);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.info("Processing thread {} interrupted", threadId);
                break;
            } catch (Exception e) {
                log.error("Error in processing thread {}", threadId, e);
                // Continue processing despite errors
                try {
                    Thread.sleep(pollIntervalMs * 2); // Back off on errors
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.debug("Processing thread {} stopped", threadId);
    }
    
    /**
     * Processes a single telemetry packet.
     * This is where the actual packet parsing and session management will happen.
     * For now, it's a placeholder that demonstrates the processing pipeline.
     */
    private void processPacket(byte[] packet, int threadId) {
        log.trace("Thread {} processing packet of {} bytes", threadId, packet.length);
        
        try {
            // TODO: Add actual packet processing logic here
            // Examples of what will go here:
            // 1. Parse packet header to determine packet type
            // 2. Route to appropriate parser (session data, lap data, car telemetry, etc.)
            // 3. Update session state
            // 4. Persist data if needed
            // 5. Notify WebSocket listeners
            
            // For now, just log that we processed it
            if (log.isTraceEnabled()) {
                log.trace("Successfully processed packet: first 4 bytes = [{}, {}, {}, {}]", 
                    packet.length > 0 ? packet[0] : 0,
                    packet.length > 1 ? packet[1] : 0, 
                    packet.length > 2 ? packet[2] : 0,
                    packet.length > 3 ? packet[3] : 0);
            }
            
        } catch (Exception e) {
            log.error("Failed to process telemetry packet", e);
            // Don't rethrow - we want to continue processing other packets
        }
    }
    
    /**
     * Gets the current status of the processing service.
     */
    public ProcessingStatus getStatus() {
        return new ProcessingStatus(
            processingEnabled,
            running.get(),
            processingThreadCount,
            pollIntervalMs,
            bufferManager.getBufferStats().currentSize()
        );
    }
    
    @PreDestroy
    public void shutdown() {
        if (processingExecutor == null) {
            return;
        }
        
        log.info("Shutting down TelemetryProcessingService");
        stopProcessing();
        
        processingExecutor.shutdown();
        try {
            if (!processingExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Processing threads did not terminate gracefully, forcing shutdown");
                processingExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            processingExecutor.shutdownNow();
        }
    }
    
    /**
     * Status information about the telemetry processing service.
     */
    public record ProcessingStatus(
        boolean enabled,
        boolean running,
        int threadCount,
        int pollIntervalMs,
        int currentBufferSize
    ) {}
}