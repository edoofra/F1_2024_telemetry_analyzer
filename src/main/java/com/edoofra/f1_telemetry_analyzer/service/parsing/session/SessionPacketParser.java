package com.edoofra.f1_telemetry_analyzer.service.parsing.session;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.model.packets.MarshalZone;
import com.edoofra.f1_telemetry_analyzer.model.packets.PacketId;
import com.edoofra.f1_telemetry_analyzer.model.packets.SessionPacketData;
import com.edoofra.f1_telemetry_analyzer.model.packets.WeatherForecastSample;
import com.edoofra.f1_telemetry_analyzer.service.parsing.PacketParser;
import com.edoofra.f1_telemetry_analyzer.service.session.SessionManagementService;
import com.edoofra.f1_telemetry_analyzer.util.BinaryDataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// Parses PacketSessionData per F1 25 UDP spec (753 bytes total, 2 Hz).
// All offsets are absolute from the start of the packet.
@Slf4j
@Service
@RequiredArgsConstructor
public class SessionPacketParser implements PacketParser<SessionPacketData> {

    private final SessionManagementService sessionManagementService;

    private static final int PACKET_SIZE = 753;

    // Size of each sub-structure
    private static final int MARSHAL_ZONE_SIZE = 5;           // float(4) + int8(1)
    private static final int WEATHER_FORECAST_SIZE = 8;       // 8 x uint8/int8

    // Offsets relative to packet start (header = 29 bytes)
    private static final int OFF_WEATHER                        = 29;
    private static final int OFF_TRACK_TEMPERATURE              = 30;
    private static final int OFF_AIR_TEMPERATURE                = 31;
    private static final int OFF_TOTAL_LAPS                     = 32;
    private static final int OFF_TRACK_LENGTH                   = 33;
    private static final int OFF_SESSION_TYPE                   = 35;
    private static final int OFF_TRACK_ID                       = 36;
    private static final int OFF_FORMULA                        = 37;
    private static final int OFF_SESSION_TIME_LEFT              = 38;
    private static final int OFF_SESSION_DURATION               = 40;
    private static final int OFF_PIT_SPEED_LIMIT                = 42;
    private static final int OFF_GAME_PAUSED                    = 43;
    private static final int OFF_IS_SPECTATING                  = 44;
    private static final int OFF_SPECTATOR_CAR_INDEX            = 45;
    private static final int OFF_NUM_MARSHAL_ZONES              = 47;
    private static final int OFF_MARSHAL_ZONES                  = 48;   // 21 * 5 = 105 bytes
    private static final int OFF_SAFETY_CAR_STATUS             = 153;
    private static final int OFF_NETWORK_GAME                   = 154;
    private static final int OFF_NUM_WEATHER_FORECAST_SAMPLES   = 155;
    private static final int OFF_WEATHER_FORECAST_SAMPLES       = 156;  // 64 * 8 = 512 bytes
    private static final int OFF_FORECAST_ACCURACY              = 668;
    private static final int OFF_AI_DIFFICULTY                  = 669;
    private static final int OFF_SEASON_LINK_IDENTIFIER         = 670;
    private static final int OFF_WEEKEND_LINK_IDENTIFIER        = 674;
    private static final int OFF_SESSION_LINK_IDENTIFIER        = 678;
    private static final int OFF_PIT_STOP_WINDOW_IDEAL_LAP      = 682;
    private static final int OFF_PIT_STOP_WINDOW_LATEST_LAP     = 683;
    private static final int OFF_PIT_STOP_REJOIN_POSITION       = 684;
    private static final int OFF_GAME_MODE                      = 694;
    private static final int OFF_RULE_SET                       = 695;
    private static final int OFF_TIME_OF_DAY                    = 696;
    private static final int OFF_SESSION_LENGTH                 = 700;
    private static final int OFF_NUM_SAFETY_CAR_PERIODS         = 705;
    private static final int OFF_NUM_VIRTUAL_SAFETY_CAR_PERIODS = 706;
    private static final int OFF_NUM_RED_FLAG_PERIODS           = 707;
    private static final int OFF_SECTOR2_LAP_DISTANCE_START     = 745;
    private static final int OFF_SECTOR3_LAP_DISTANCE_START     = 749;

    @Override
    public PacketId getSupportedPacketId() {
        return PacketId.SESSION;
    }

    @Override
    public SessionPacketData parse(byte[] data, TelemetryPacketHeader header) {
        if (data.length < PACKET_SIZE) {
            log.warn("Session packet too small: {} bytes (expected {})", data.length, PACKET_SIZE);
            return null;
        }

        try {
            int numMarshalZones = data[OFF_NUM_MARSHAL_ZONES] & 0xFF;
            int numForecastSamples = data[OFF_NUM_WEATHER_FORECAST_SAMPLES] & 0xFF;

            SessionPacketData packet = new SessionPacketData(
                    header,
                    data[OFF_WEATHER] & 0xFF,
                    data[OFF_TRACK_TEMPERATURE],
                    data[OFF_AIR_TEMPERATURE],
                    data[OFF_TOTAL_LAPS] & 0xFF,
                    BinaryDataUtils.getUnsignedShort(data, OFF_TRACK_LENGTH),
                    data[OFF_SESSION_TYPE] & 0xFF,
                    data[OFF_TRACK_ID],
                    data[OFF_FORMULA] & 0xFF,
                    BinaryDataUtils.getUnsignedShort(data, OFF_SESSION_TIME_LEFT),
                    BinaryDataUtils.getUnsignedShort(data, OFF_SESSION_DURATION),
                    data[OFF_PIT_SPEED_LIMIT] & 0xFF,
                    data[OFF_GAME_PAUSED] != 0,
                    data[OFF_IS_SPECTATING] != 0,
                    data[OFF_SPECTATOR_CAR_INDEX] & 0xFF,
                    numMarshalZones,
                    parseMarshalZones(data, numMarshalZones),
                    data[OFF_SAFETY_CAR_STATUS] & 0xFF,
                    data[OFF_NETWORK_GAME] != 0,
                    numForecastSamples,
                    parseWeatherForecastSamples(data, numForecastSamples),
                    data[OFF_FORECAST_ACCURACY] & 0xFF,
                    data[OFF_AI_DIFFICULTY] & 0xFF,
                    BinaryDataUtils.getUnsignedInt(data, OFF_SEASON_LINK_IDENTIFIER),
                    BinaryDataUtils.getUnsignedInt(data, OFF_WEEKEND_LINK_IDENTIFIER),
                    BinaryDataUtils.getUnsignedInt(data, OFF_SESSION_LINK_IDENTIFIER),
                    data[OFF_PIT_STOP_WINDOW_IDEAL_LAP] & 0xFF,
                    data[OFF_PIT_STOP_WINDOW_LATEST_LAP] & 0xFF,
                    data[OFF_PIT_STOP_REJOIN_POSITION] & 0xFF,
                    data[OFF_GAME_MODE] & 0xFF,
                    data[OFF_RULE_SET] & 0xFF,
                    BinaryDataUtils.getUnsignedInt(data, OFF_TIME_OF_DAY),
                    data[OFF_SESSION_LENGTH] & 0xFF,
                    data[OFF_NUM_SAFETY_CAR_PERIODS] & 0xFF,
                    data[OFF_NUM_VIRTUAL_SAFETY_CAR_PERIODS] & 0xFF,
                    data[OFF_NUM_RED_FLAG_PERIODS] & 0xFF,
                    BinaryDataUtils.getFloat(data, OFF_SECTOR2_LAP_DISTANCE_START),
                    BinaryDataUtils.getFloat(data, OFF_SECTOR3_LAP_DISTANCE_START)
            );

            log.debug("Session packet: track={}, type={}, timeLeft={}s, weather={}, SC={}",
                    packet.trackId(), packet.sessionType(), packet.sessionTimeLeft(),
                    packet.weather(), packet.safetyCarStatus());

            sessionManagementService.process(packet);
            return packet;

        } catch (Exception e) {
            log.error("Failed to parse session packet", e);
            return null;
        }
    }

    private List<MarshalZone> parseMarshalZones(byte[] data, int count) {
        List<MarshalZone> zones = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int offset = OFF_MARSHAL_ZONES + i * MARSHAL_ZONE_SIZE;
            zones.add(new MarshalZone(
                    BinaryDataUtils.getFloat(data, offset),
                    data[offset + 4]
            ));
        }
        return zones;
    }

    private List<WeatherForecastSample> parseWeatherForecastSamples(byte[] data, int count) {
        List<WeatherForecastSample> samples = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int offset = OFF_WEATHER_FORECAST_SAMPLES + i * WEATHER_FORECAST_SIZE;
            samples.add(new WeatherForecastSample(
                    data[offset] & 0xFF,
                    data[offset + 1] & 0xFF,
                    data[offset + 2] & 0xFF,
                    data[offset + 3],
                    data[offset + 4],
                    data[offset + 5],
                    data[offset + 6],
                    data[offset + 7] & 0xFF
            ));
        }
        return samples;
    }
}
