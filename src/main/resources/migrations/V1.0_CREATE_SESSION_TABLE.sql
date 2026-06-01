CREATE TABLE IF NOT EXISTS session (
    id UUID PRIMARY KEY,
    game_session_id TEXT UNIQUE,
    type TEXT,
    track_name TEXT,
    weather TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_session_type ON session(type);
CREATE INDEX IF NOT EXISTS idx_session_track_name ON session(track_name);