package com.edoofra.f1_telemetry_analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the telemetry processing system.
 * This class centralizes all processing-related configuration properties
 * and provides validation and default values.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "f1.telemetry.processing")
public class TelemetryProcessingConfig {
    
    /**
     * Whether telemetry processing is enabled.
     * When disabled, packets will accumulate in the buffer but won't be processed.
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Number of background threads dedicated to processing telemetry packets.
     * More threads can handle higher packet volumes but use more CPU resources.
     * For F1 telemetry at 60Hz, 1-2 threads are typically sufficient.
     * Default: 1
     */
    private int threadCount = 1;
    
    /**
     * Interval in milliseconds between buffer polls when no packets are available.
     * Lower values provide better responsiveness but use more CPU.
     * Higher values reduce CPU usage but may introduce processing delays.
     * Default: 10ms (suitable for 60Hz F1 telemetry)
     */
    private int pollIntervalMs = 10;
    
    /**
     * Maximum time in milliseconds a processing thread will wait for a packet
     * before checking if it should continue running. This affects shutdown responsiveness.
     * Default: 100ms
     */
    private int maxWaitTimeMs = 100;
    
    /**
     * Whether to process packets in batch mode for better efficiency.
     * When enabled, multiple packets are processed in a single operation.
     * Default: false (process one packet at a time for better real-time response)
     */
    private boolean batchProcessing = false;
    
    /**
     * Maximum number of packets to process in a single batch when batch processing is enabled.
     * Only applicable when batchProcessing is true.
     * Default: 10
     */
    private int batchSize = 10;
    
    /**
     * Whether to enable detailed performance metrics for packet processing.
     * When enabled, tracks processing times, throughput, and error rates per thread.
     * Default: true
     */
    private boolean enableMetrics = true;
    
    /**
     * Interval in seconds for logging processing statistics.
     * Default: 30 seconds
     */
    private int statsLogIntervalSeconds = 30;
}