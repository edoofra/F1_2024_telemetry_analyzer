package com.edoofra.f1_telemetry_analyzer.controller;

import com.edoofra.f1_telemetry_analyzer.model.BufferStats;
import com.edoofra.f1_telemetry_analyzer.service.processing.TelemetryProcessingService;
import com.edoofra.f1_telemetry_analyzer.service.udp.TelemetryBufferManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for monitoring and controlling the F1 telemetry system.
 * Provides endpoints to check buffer status, processing status, and control operations.
 */
@RestController
@RequestMapping("/api/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryBufferManager bufferManager;
    private final TelemetryProcessingService processingService;
    
    /**
     * Get current buffer statistics.
     */
    @GetMapping("/buffer/stats")
    public ResponseEntity<BufferStats> getBufferStats() {
        return ResponseEntity.ok(bufferManager.getBufferStats());
    }
    
    /**
     * Get processing service status.
     */
    @GetMapping("/processing/status")
    public ResponseEntity<TelemetryProcessingService.ProcessingStatus> getProcessingStatus() {
        return ResponseEntity.ok(processingService.getStatus());
    }
    
    /**
     * Start telemetry processing.
     */
    @PostMapping("/processing/start")
    public ResponseEntity<String> startProcessing() {
        processingService.startProcessing();
        return ResponseEntity.ok("Telemetry processing started");
    }
    
    /**
     * Stop telemetry processing.
     */
    @PostMapping("/processing/stop")
    public ResponseEntity<String> stopProcessing() {
        processingService.stopProcessing();
        return ResponseEntity.ok("Telemetry processing stopped");
    }
    
    /**
     * Clear the telemetry buffer.
     */
    @PostMapping("/buffer/clear")
    public ResponseEntity<String> clearBuffer() {
        bufferManager.clearBuffer();
        return ResponseEntity.ok("Telemetry buffer cleared");
    }
    
    /**
     * Get system health status.
     */
    @GetMapping("/health")
    public ResponseEntity<SystemHealth> getHealth() {
        BufferStats bufferStats = bufferManager.getBufferStats();
        TelemetryProcessingService.ProcessingStatus processingStatus = processingService.getStatus();
        
        return ResponseEntity.ok(new SystemHealth(
            bufferStats,
            processingStatus,
            determineOverallHealth(bufferStats, processingStatus)
        ));
    }
    
    private String determineOverallHealth(BufferStats bufferStats, TelemetryProcessingService.ProcessingStatus processingStatus) {
        if (!processingStatus.enabled()) {
            return "DISABLED";
        }
        
        if (!processingStatus.running()) {
            return "STOPPED";
        }
        
        if (bufferStats.dropRate() > 5.0) {
            return "DEGRADED";
        }
        
        return "HEALTHY";
    }
    
    /**
     * Overall system health information.
     */
    public record SystemHealth(
        BufferStats bufferStats,
        TelemetryProcessingService.ProcessingStatus processingStatus,
        String overallHealth
    ) {}
}