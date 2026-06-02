package com.edoofra.f1_telemetry_analyzer.model.packets;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum PacketId {
    MOTION(0),
    SESSION(1),
    LAP_DATA(2),
    EVENT(3),
    PARTICIPANTS(4),
    CAR_SETUPS(5),
    CAR_TELEMETRY(6),
    CAR_STATUS(7),
    FINAL_CLASSIFICATION(8),
    LOBBY_INFO(9),
    CAR_DAMAGE(10),
    SESSION_HISTORY(11),
    TYRE_SETS(12),
    MOTION_EX(13),
    TIME_TRIAL(14),
    LAP_POSITIONS(15);

    private final int id;
    private static final Map<Integer, PacketId> BY_ID = Arrays.stream(values())
            .collect(Collectors.toMap(p -> p.id, p -> p));

    PacketId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PacketId fromId(int id) {
        return BY_ID.get(id);
    }
}
