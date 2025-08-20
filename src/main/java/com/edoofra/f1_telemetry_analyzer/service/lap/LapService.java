package com.edoofra.f1_telemetry_analyzer.service.lap;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import com.edoofra.f1_telemetry_analyzer.persistence.repository.LapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Service for managing lap data in F1 telemetry analysis.
 * Handles saving lap data to the database and avoiding duplicates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LapService {

    private final LapRepository lapRepository;
    private final LapDataMergeService lapDataMergeService;

    /**
     * Save lap data to the database.
     * Updates existing lap with new cumulative data if lap already exists.
     */
    @Transactional
    public Lap saveLapData(Lap lapData) {
        try {
            if (lapData.getSessionId() != null && lapData.getLapNumber() != null) {
                var existingLaps = lapRepository.findBySessionIdAndLapNumber(
                        lapData.getSessionId(), lapData.getLapNumber());

                if (!existingLaps.isEmpty()) {
                    return mergeAndUpdateLap(lapData, existingLaps);
                }
            }

            Lap savedLap = lapRepository.save(lapData);
            log.debug("Saved new lap data: session={}, lap={}, time={}ms",
                    savedLap.getSessionId(), savedLap.getLapNumber(), savedLap.getLapTimeMs());
            return savedLap;

        } catch (Exception e) {
            log.error("Failed to save lap data", e);
            return null;
        }
    }

    private Lap mergeAndUpdateLap(Lap lapData, List<Lap> existingLaps) {
        return Optional.of(existingLaps.get(0))
                .map(existingLap -> lapDataMergeService.mergeLapData(existingLap, lapData))
                .map(lapRepository::save)
                .orElseThrow();
    }
}