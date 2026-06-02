package com.edoofra.f1_telemetry_analyzer.util;

import java.util.Map;

public final class TrackNames {

    // F1 25 track IDs → circuit names (from spec appendix)
    private static final Map<Integer, String> NAMES = Map.ofEntries(
            Map.entry(0,  "Melbourne"),
            Map.entry(2,  "Shanghai"),
            Map.entry(3,  "Sakhir"),
            Map.entry(4,  "Catalunya"),
            Map.entry(5,  "Monaco"),
            Map.entry(6,  "Montreal"),
            Map.entry(7,  "Silverstone"),
            Map.entry(9,  "Hungaroring"),
            Map.entry(10, "Spa"),
            Map.entry(11, "Monza"),
            Map.entry(12, "Singapore"),
            Map.entry(13, "Suzuka"),
            Map.entry(14, "Abu Dhabi"),
            Map.entry(15, "COTA"),
            Map.entry(16, "Interlagos"),
            Map.entry(17, "Red Bull Ring"),
            Map.entry(19, "Mexico City"),
            Map.entry(20, "Baku"),
            Map.entry(26, "Zandvoort"),
            Map.entry(27, "Imola"),
            Map.entry(29, "Jeddah"),
            Map.entry(30, "Miami"),
            Map.entry(31, "Las Vegas"),
            Map.entry(32, "Losail"),
            Map.entry(39, "Silverstone Reverse"),
            Map.entry(40, "Red Bull Ring Reverse"),
            Map.entry(41, "Zandvoort Reverse")
    );

    private TrackNames() {}

    public static String fromId(int id) {
        return NAMES.getOrDefault(id, "Unknown");
    }
}
