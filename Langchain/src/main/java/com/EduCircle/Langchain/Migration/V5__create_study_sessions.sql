-- V5__create_study_sessions.sql
CREATE TABLE IF NOT EXISTS study_sessions (
    id           BIGSERIAL   PRIMARY KEY,
    user_id      BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_id      BIGINT      REFERENCES notes(id) ON DELETE SET NULL,
    session_type VARCHAR(30)
                 CHECK (session_type IN ('FLASHCARD','QUIZ','READING','ASK','VOICE')),
    duration_sec INT,
    score        INT,
    started_at   TIMESTAMPTZ DEFAULT NOW(),
    ended_at     TIMESTAMPTZ
);

CREATE INDEX idx_sessions_user_date ON study_sessions(user_id, started_at);