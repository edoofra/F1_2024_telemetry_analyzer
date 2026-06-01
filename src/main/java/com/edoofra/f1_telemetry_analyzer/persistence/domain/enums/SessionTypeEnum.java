package com.edoofra.f1_telemetry_analyzer.persistence.domain.enums;

// Maps to F1 25 session type IDs (0-18) — see PacketSessionData spec.
public enum SessionTypeEnum {
    UNKNOWN,
    PRACTICE_1,
    PRACTICE_2,
    PRACTICE_3,
    SHORT_PRACTICE,
    QUALIFYING_1,
    QUALIFYING_2,
    QUALIFYING_3,
    SHORT_QUALIFYING,
    ONE_SHOT_QUALIFYING,
    SPRINT_SHOOTOUT_1,
    SPRINT_SHOOTOUT_2,
    SPRINT_SHOOTOUT_3,
    SHORT_SPRINT_SHOOTOUT,
    ONE_SHOT_SPRINT_SHOOTOUT,
    RACE,
    RACE_2,
    RACE_3,
    TIME_TRIAL
}
