package com.edoofra.f1_telemetry_analyzer.service.parsing.parser;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.Lap;
import com.edoofra.f1_telemetry_analyzer.persistence.domain.Session;
import com.edoofra.f1_telemetry_analyzer.util.BinaryDataUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class LapDataParser {

    // Lap data field offsets within each lap data entry
    private static final int LAST_LAP_TIME_OFFSET = 0;     // uint32
    private static final int CURRENT_LAP_TIME_OFFSET = 4;   // uint32
    private static final int SECTOR1_TIME_OFFSET = 8;      // uint16
    private static final int SECTOR1_TIME_MINUTES_OFFSET = 10; // uint8
    private static final int SECTOR2_TIME_OFFSET = 11;     // uint16
    private static final int SECTOR2_TIME_MINUTES_OFFSET = 13; // uint8
    private static final int DELTA_TO_CAR_IN_FRONT_OFFSET = 14; // uint16
    private static final int DELTA_TO_RACE_LEADER_OFFSET = 16; // uint16
    private static final int LAP_DISTANCE_OFFSET = 18;     // float
    private static final int TOTAL_DISTANCE_OFFSET = 22;   // float
    private static final int SAFETY_CAR_DELTA_OFFSET = 26; // float
    private static final int CAR_POSITION_OFFSET = 30;     // uint8
    private static final int CURRENT_LAP_NUM_OFFSET = 31;  // uint8
    private static final int PIT_STATUS_OFFSET = 32;       // uint8
    private static final int NUM_PIT_STOPS_OFFSET = 33;    // uint8
    private static final int SECTOR_OFFSET = 34;           // uint8
    private static final int CURRENT_LAP_INVALID_OFFSET = 35; // uint8
    private static final int PENALTIES_OFFSET = 36;        // uint8
    private static final int WARNINGS_OFFSET = 37;         // uint8
    private static final int NUM_UNSERVED_DRIVE_THROUGH_PENS_OFFSET = 38; // uint8
    private static final int NUM_UNSERVED_STOP_GO_PENS_OFFSET = 39; // uint8
    private static final int GRID_POSITION_OFFSET = 40;    // uint8
    private static final int DRIVER_STATUS_OFFSET = 41;    // uint8
    private static final int RESULT_STATUS_OFFSET = 42;    // uint8
    private static final int PIT_LANE_TIMER_ACTIVE_OFFSET = 43; // uint8
    private static final int PIT_LANE_TIME_IN_LANE_OFFSET = 44; // uint16
    private static final int PIT_STOP_TIMER_OFFSET = 46;   // uint16
    private static final int PIT_STOP_SHOULD_SERVE_PEN_OFFSET = 48; // uint8

    /**
     * Parse lap data from the binary packet data.
     */
    public static Lap parseLapDataFromBytes(byte[] packetData, int offset, Session session) {
        try {
            // Extract lap timing data
            long lastLapTimeMs = BinaryDataUtils.readUInt32(packetData, offset + LAST_LAP_TIME_OFFSET);

            // Sector times (in milliseconds, but stored as uint16 + uint8 for minutes)
            int sector1TimeMs = BinaryDataUtils.readUInt16(packetData, offset + SECTOR1_TIME_OFFSET);
            int sector1Minutes = BinaryDataUtils.readUInt8(packetData, offset + SECTOR1_TIME_MINUTES_OFFSET);
            int totalSector1TimeMs = (sector1Minutes * 60 * 1000) + sector1TimeMs;

            int sector2TimeMs = BinaryDataUtils.readUInt16(packetData, offset + SECTOR2_TIME_OFFSET);
            int sector2Minutes = BinaryDataUtils.readUInt8(packetData, offset + SECTOR2_TIME_MINUTES_OFFSET);
            int totalSector2TimeMs = (sector2Minutes * 60 * 1000) + sector2TimeMs;

            // Calculate sector 3 time (total lap time - sector 1 - sector 2)
            int sector3TimeMs = 0;
            if (lastLapTimeMs > 0 && totalSector1TimeMs > 0 && totalSector2TimeMs > 0) {
                sector3TimeMs = (int)(lastLapTimeMs - totalSector1TimeMs - totalSector2TimeMs);
            }

            // Position and distance data
            float lapDistance = BinaryDataUtils.readFloat(packetData, offset + LAP_DISTANCE_OFFSET);
            float totalDistance = BinaryDataUtils.readFloat(packetData, offset + TOTAL_DISTANCE_OFFSET);
            int carPosition = BinaryDataUtils.readUInt8(packetData, offset + CAR_POSITION_OFFSET);
            int currentLapNumber = BinaryDataUtils.readUInt8(packetData, offset + CURRENT_LAP_NUM_OFFSET);

            // Create lap entity
            return Lap.builder()
                    .withSessionId(session.getId())
                    .withLapNumber(currentLapNumber)
                    .withLapTimeMs((int)lastLapTimeMs)
                    .withSector1TimeMs(totalSector1TimeMs > 0 ? totalSector1TimeMs : null)
                    .withSector2TimeMs(totalSector2TimeMs > 0 ? totalSector2TimeMs : null)
                    .withSector3TimeMs(sector3TimeMs > 0 ? sector3TimeMs : null)
                    .withRacePosition(carPosition > 0 ? carPosition : null)
                    .withLapDistanceM((int)lapDistance)
                    .withTotalDistanceM((int)totalDistance)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing lap data from bytes", e);
            return null;
        }
    }
}
