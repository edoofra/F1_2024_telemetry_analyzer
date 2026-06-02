package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.model.packets.PacketId;

public interface PacketParser<T> {

    T parse(byte[] data, TelemetryPacketHeader header);

    PacketId getSupportedPacketId();
}
