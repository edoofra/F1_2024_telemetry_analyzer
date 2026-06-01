package com.edoofra.f1_telemetry_analyzer.service.parsing;

import com.edoofra.f1_telemetry_analyzer.model.TelemetryPacketHeader;
import com.edoofra.f1_telemetry_analyzer.model.packets.PacketId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PacketDispatcher {

    private final HeaderParsingService headerParsingService;
    private final Map<PacketId, PacketParser<?>> parsers;

    public PacketDispatcher(HeaderParsingService headerParsingService, List<PacketParser<?>> parserList) {
        this.headerParsingService = headerParsingService;
        this.parsers = parserList.stream()
                .collect(Collectors.toMap(PacketParser::getSupportedPacketId, p -> p));
        log.info("Registered packet parsers: {}", parsers.keySet());
    }

    public void dispatch(byte[] data) {
        TelemetryPacketHeader header = headerParsingService.parseHeader(data);
        if (header == null) {
            return;
        }

        PacketId packetId = PacketId.fromId(header.packetId());
        if (packetId == null) {
            log.debug("Unknown packet ID: {}", header.packetId());
            return;
        }

        PacketParser<?> parser = parsers.get(packetId);
        if (parser == null) {
            log.trace("No parser registered for packet type: {}", packetId);
            return;
        }

        parser.parse(data, header);
    }
}
