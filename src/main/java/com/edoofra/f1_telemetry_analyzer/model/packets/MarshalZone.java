package com.edoofra.f1_telemetry_analyzer.model.packets;

// zoneFlag: -1 = invalid, 0 = none, 1 = green, 2 = blue, 3 = yellow
public record MarshalZone(float zoneStart, int zoneFlag) {}
