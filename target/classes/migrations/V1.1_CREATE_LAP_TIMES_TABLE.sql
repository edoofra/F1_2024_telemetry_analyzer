CREATE TABLE IF NOT EXISTS lap (
    id UUID PRIMARY KEY,
    session_id UUID NOT NULL,
    lap_number INTEGER,
    lap_time_ms INTEGER NOT NULL,
    sector_1_time_ms INTEGER,
    sector_2_time_ms INTEGER,
    sector_3_time_ms INTEGER,
    race_position INTEGER,
    lap_distance_m INTEGER,
    total_distance_m INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (session_id) REFERENCES session(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_lap_session_id_lap_number ON lap(session_id, lap_number);
CREATE INDEX IF NOT EXISTS idx_lap_session_id ON lap(session_id);
CREATE INDEX IF NOT EXISTS idx_lap_lap_time_ms ON lap(lap_time_ms);