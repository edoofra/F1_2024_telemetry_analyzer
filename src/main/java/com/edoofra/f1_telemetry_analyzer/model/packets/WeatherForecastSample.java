package com.edoofra.f1_telemetry_analyzer.model.packets;

// weather: 0=clear, 1=light cloud, 2=overcast, 3=light rain, 4=heavy rain, 5=storm
// trackTemperatureChange / airTemperatureChange: 0=up, 1=down, 2=no change
public record WeatherForecastSample(
        int sessionType,
        int timeOffset,
        int weather,
        int trackTemperature,
        int trackTemperatureChange,
        int airTemperature,
        int airTemperatureChange,
        int rainPercentage
) {}
