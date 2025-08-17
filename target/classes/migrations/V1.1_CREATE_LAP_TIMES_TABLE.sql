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