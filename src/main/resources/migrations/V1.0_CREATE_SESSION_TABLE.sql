CREATE TABLE IF NOT EXISTS session (
    id UUID PRIMARY KEY,
    game_session_id TEXT,
    car_id TEXT,
    type TEXT,
    track_name TEXT,
    weather TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NOT NULL,
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_session_game_session_id ON session(game_session_id);
CREATE INDEX IF NOT EXISTS idx_session_car_id ON session(car_id);
CREATE INDEX IF NOT EXISTS idx_session_type ON session(type);
CREATE INDEX IF NOT EXISTS idx_session_track_name ON session(track_name);