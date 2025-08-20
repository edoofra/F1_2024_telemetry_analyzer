package com.edoofra.f1_telemetry_analyzer.service.lap;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
public class LapDataMergeService {

    /**
     * Merge new lap data with existing lap data, updating non-null fields.
     * This handles the cumulative nature of F1 telemetry data.
     */
    public Lap mergeLapData(Lap existingLap, Lap newLapData) {
        // Merge timing data
        mergeValidValue(newLapData, Lap::getLapTimeMs, existingLap::setLapTimeMs);
        mergeValidValue(newLapData, Lap::getSector1TimeMs, existingLap::setSector1TimeMs);
        mergeValidValue(newLapData, Lap::getSector2TimeMs, existingLap::setSector2TimeMs);
        mergeValidValue(newLapData, Lap::getSector3TimeMs, existingLap::setSector3TimeMs);
        // Merge position and distance data
        mergeValidValue(newLapData, Lap::getRacePosition, existingLap::setRacePosition);
        mergeValidValue(newLapData, Lap::getLapDistanceM, existingLap::setLapDistanceM);
        mergeValidValue(newLapData, Lap::getTotalDistanceM, existingLap::setTotalDistanceM);
        
        return existingLap;
    }
    
    /**
     * Merge a single field if the new value is valid (non-null and positive).
     * Uses Optional to handle null values elegantly.
     */
    private <T extends Number> void mergeValidValue(
            Lap newLapData, 
            Function<Lap, T> getter, 
            Consumer<T> setter) {
        
        Optional.ofNullable(getter.apply(newLapData))
                .filter(value -> isValidValue(value))
                .ifPresent(setter);
    }
    
    /**
     * Check if a numeric value is valid (positive).
     * Handles different numeric types consistently.
     */
    private boolean isValidValue(Number value) {
        return value != null && value.doubleValue() > 0;
    }
}
