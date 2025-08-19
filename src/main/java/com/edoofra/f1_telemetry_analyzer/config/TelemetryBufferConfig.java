package com.edoofra.f1_telemetry_analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the telemetry buffer management system.
 * This class centralizes all buffer-related configuration properties
 * and provides validation and default values.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "f1.telemetry.buffer")
public class TelemetryBufferConfig {
    
    /**
     * The maximum number of telemetry packets the ring buffer can hold.
     * Once this limit is reached, the oldest packets will be overwritten.
     * Default: 10,000 packets
     */
    private int capacity = 10000;
    
    /**
     * Interval in seconds for logging buffer statistics.
     * This determines how frequently buffer utilization metrics are logged.
     * Default: 10 seconds
     */
    private int statsInterval = 10;
    
    /**
     * Whether to enable detailed packet size statistics.
     * When enabled, tracks distribution of packet sizes for analysis.
     * Default: false (disabled for performance)
     */
    private boolean enablePacketSizeStats = false;
    
    /**
     * Maximum time in milliseconds to wait when buffer operations are blocked.
     * This prevents indefinite blocking during high-load scenarios.
     * Default: 100ms
     */
    private int operationTimeoutMs = 100;
    
    /**
     * Threshold percentage for logging warnings when buffer utilization is high.
     * When buffer usage exceeds this percentage, warnings will be logged.
     * Default: 80% (0.8)
     */
    private double highUtilizationThreshold = 0.8;
}