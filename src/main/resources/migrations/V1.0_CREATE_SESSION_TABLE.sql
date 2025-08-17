CREATE TABLE IF NOT EXISTS session (
    id UUID PRIMARY KEY,
    game_session_id TEXT,
    type TEXT,
    track_name TEXT,
    weather TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NOT NULL,
);