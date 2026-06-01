package com.edoofra.f1_telemetry_analyzer.model.packets;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;

import java.util.List;

// Parsed representation of PacketSessionData from the F1 25 UDP spec.
// Frequency: 2 per second. Total packet size: 753 bytes.
public record SessionPacketData(
        TelemetryPacketHeader header,

        // Session conditions
        int weather,                  // 0=clear, 1=light cloud, 2=overcast, 3=light rain, 4=heavy rain, 5=storm
        int trackTemperature,         // degrees Celsius (signed)
        int airTemperature,           // degrees Celsius (signed)
        int totalLaps,
        int trackLength,              // metres
        int sessionType,              // see appendix: 0=unknown, 1=P1..4=Short P, 5-9=Qualifying, 10-14=Sprint, 15-17=Race, 18=Time Trial
        int trackId,                  // -1=unknown; see appendix for track list
        int formula,                  // 0=F1 Modern, 1=F1 Classic, 2=F2, 3=F1 Generic

        // Timing
        int sessionTimeLeft,          // seconds remaining
        int sessionDuration,          // total session length in seconds

        // Pit
        int pitSpeedLimit,            // km/h

        // Flags
        boolean gamePaused,
        boolean isSpectating,
        int spectatorCarIndex,

        // Marshal zones
        int numMarshalZones,
        List<MarshalZone> marshalZones,

        // Safety car
        int safetyCarStatus,          // 0=none, 1=full, 2=virtual, 3=formation lap
        boolean networkGame,

        // Weather forecast
        int numWeatherForecastSamples,
        List<WeatherForecastSample> weatherForecastSamples,
        int forecastAccuracy,         // 0=perfect, 1=approximate
        int aiDifficulty,             // 0-110

        // Session identifiers (persist across saves)
        long seasonLinkIdentifier,
        long weekendLinkIdentifier,
        long sessionLinkIdentifier,

        // Strategy
        int pitStopWindowIdealLap,
        int pitStopWindowLatestLap,
        int pitStopRejoinPosition,

        // Game settings
        int gameMode,
        int ruleSet,
        long timeOfDay,               // minutes since midnight
        int sessionLength,            // 0=none, 2=very short, 3=short, 4=medium, 5=medium long, 6=long, 7=full

        // Incident counts
        int numSafetyCarPeriods,
        int numVirtualSafetyCarPeriods,
        int numRedFlagPeriods,

        // Track geometry
        float sector2LapDistanceStart, // metres around track
        float sector3LapDistanceStart
) {}
