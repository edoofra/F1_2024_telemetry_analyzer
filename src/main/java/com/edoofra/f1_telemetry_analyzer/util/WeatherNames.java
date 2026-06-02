package com.edoofra.f1_telemetry_analyzer.util;

public final class WeatherNames {

    // Index == weather ID from F1 25 spec
    private static final String[] NAMES = {
            "Clear", "Light Cloud", "Overcast", "Light Rain", "Heavy Rain", "Storm"
    };

    private WeatherNames() {}

    public static String fromId(int id) {
        if (id < 0 || id >= NAMES.length) return "Unknown";
        return NAMES[id];
    }
}
