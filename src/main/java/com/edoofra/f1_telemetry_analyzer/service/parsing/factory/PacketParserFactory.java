package com.edoofra.f1_telemetry_analyzer.service.parsing.factory;

import com.edoofra.f1_telemetry_analyzer.service.parsing.TelemetryParsingService;
import com.edoofra.f1_telemetry_analyzer.service.parsing.strategy.PacketParsingStrategy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Factory for creating appropriate packet parsing strategies based on packet type.
 * 
 * This factory automatically discovers all PacketParsingStrategy implementations
 * and routes packets to the correct parser based on the packet ID.
 */
@Slf4j
@Component
public class PacketParserFactory {
    
    private final Map<Integer, PacketParsingStrategy> strategyMap = new HashMap<>();
    private final Map<Integer, AtomicLong> parsingCounters = new ConcurrentHashMap<>();
    private final AtomicLong totalParsed = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    
    @Autowired(required = false)
    private List<PacketParsingStrategy> availableStrategies;
    
    @PostConstruct
    public void initialize() {
        if (availableStrategies == null || availableStrategies.isEmpty()) {
            log.warn("No packet parsing strategies found. Telemetry parsing will be limited.");
            return;
        }
        
        // Register all available strategies
        for (PacketParsingStrategy strategy : availableStrategies) {
            int packetId = strategy.getSupportedPacketId();
            strategyMap.put(packetId, strategy);
            parsingCounters.put(packetId, new AtomicLong(0));
            
            log.info("Registered parsing strategy '{}' for packet ID {}", 
                strategy.getStrategyName(), packetId);
        }
        
        log.info("PacketParserFactory initialized with {} parsing strategies", strategyMap.size());
    }
    
    /**
     * Get the appropriate parsing strategy for the given packet type.
     * 
     * @param packetId F1 telemetry packet ID
     * @return parsing strategy or null if no strategy exists for this packet type
     */
    public PacketParsingStrategy getParsingStrategy(int packetId) {
        PacketParsingStrategy strategy = strategyMap.get(packetId);
        
        if (strategy != null) {
            // Increment parsing counter for this packet type
            parsingCounters.get(packetId).incrementAndGet();
            totalParsed.incrementAndGet();
        }
        
        return strategy;
    }
    
    /**
     * Report a parsing error for statistics.
     */
    public void reportParsingError() {
        totalErrors.incrementAndGet();
    }
    
    /**
     * Get parsing statistics for monitoring.
     */
    public TelemetryParsingService.ParsingStats getParsingStats() {
        // Create array of parsing counts by packet type
        int maxPacketId = strategyMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        long[] parsedByType = new long[maxPacketId + 1];
        
        for (Map.Entry<Integer, AtomicLong> entry : parsingCounters.entrySet()) {
            parsedByType[entry.getKey()] = entry.getValue().get();
        }
        
        return new TelemetryParsingService.ParsingStats(
            totalParsed.get(),
            totalErrors.get(),
            parsedByType
        );
    }
    
    /**
     * Get all supported packet IDs.
     */
    public int[] getSupportedPacketIds() {
        return strategyMap.keySet().stream().mapToInt(Integer::intValue).toArray();
    }
    
    /**
     * Check if a packet type is supported.
     */
    public boolean isPacketTypeSupported(int packetId) {
        return strategyMap.containsKey(packetId);
    }
    
    /**
     * Get the strategy name for a given packet ID.
     */
    public String getStrategyName(int packetId) {
        PacketParsingStrategy strategy = strategyMap.get(packetId);
        return strategy != null ? strategy.getStrategyName() : "Unknown";
    }
}