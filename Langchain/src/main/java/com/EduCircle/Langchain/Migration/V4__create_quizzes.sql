-- V4__create_quizzes.sql
CREATE TABLE IF NOT EXISTS quizzes (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_id      BIGINT       REFERENCES notes(id) ON DELETE SET NULL,
    title        VARCHAR(500),
    questions    JSONB        NOT NULL,
    score        INT,
    max_score    INT,
    quiz_type    VARCHAR(20)  DEFAULT 'MCQ'
                 CHECK (quiz_type IN ('MCQ','TRUE_FALSE','SHORT_ANSWER','MIXED')),
    attempted_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_quizzes_user_id ON quizzes(user_id);