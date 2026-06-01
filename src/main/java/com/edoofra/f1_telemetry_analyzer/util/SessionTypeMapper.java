package com.edoofra.f1_telemetry_analyzer.util;

import com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum;

import static com.edoofra.f1_telemetry_analyzer.persistence.domain.enums.SessionTypeEnum.*;

public final class SessionTypeMapper {

    // Index == F1 25 session type ID (0-18)
    private static final SessionTypeEnum[] BY_ID = {
            UNKNOWN, PRACTICE_1, PRACTICE_2, PRACTICE_3, SHORT_PRACTICE,
            QUALIFYING_1, QUALIFYING_2, QUALIFYING_3, SHORT_QUALIFYING, ONE_SHOT_QUALIFYING,
            SPRINT_SHOOTOUT_1, SPRINT_SHOOTOUT_2, SPRINT_SHOOTOUT_3,
            SHORT_SPRINT_SHOOTOUT, ONE_SHOT_SPRINT_SHOOTOUT,
            RACE, RACE_2, RACE_3, TIME_TRIAL
    };

    private SessionTypeMapper() {}

    public static SessionTypeEnum fromId(int id) {
        if (id < 0 || id >= BY_ID.length) return UNKNOWN;
        return BY_ID[id];
    }
}
