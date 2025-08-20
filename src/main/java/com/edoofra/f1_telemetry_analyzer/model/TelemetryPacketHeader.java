package com.edoofra.f1_telemetry_analyzer.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Represents the header of a telemetry packet.
 * This record contains metadata about the packet, such as format, version, session ID, and player car index.
 * packetFormat: The format of the packet.
 * gameMajorVersion: The major version of the game.
 * gameMinorVersion: The minor version of the game.
 * packetVersion: The version of the packet format.
 * packetId: The ID of the packet type.
 * sessionUID: Unique identifier for the session.
 * sessionTime: The time in seconds since the session started.
 * frameIdentifier: Identifier for the frame, used to track the sequence of packets.
 * playerCarIndex: The index of the player's car in the session.
 * secondaryPlayerCarIndex: The index of the secondary player's car, if applicable.
 */
@Builder(toBuilder = true, setterPrefix = "with")
public record TelemetryPacketHeader(int packetFormat,
                                    int gameMajorVersion,
                                    int gameMinorVersion,
                                    int packetVersion,
                                    int packetId,
                                    long sessionUID,
                                    float sessionTime,
                                    int frameIdentifier,
                                    int playerCarIndex,
                                    int secondaryPlayerCarIndex) {
}