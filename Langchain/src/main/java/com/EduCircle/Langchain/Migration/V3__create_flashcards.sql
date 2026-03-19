-- V3__create_flashcards.sql
CREATE TABLE IF NOT EXISTS flashcards (
    id               BIGSERIAL    PRIMARY KEY,
    user_id          BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    note_id          BIGINT       REFERENCES notes(id) ON DELETE SET NULL,
    question         TEXT         NOT NULL,
    answer           TEXT         NOT NULL,
    repetitions      INT          DEFAULT 0,
    ease_factor      DECIMAL(4,2) DEFAULT 2.50,
    interval_days    INT          DEFAULT 1,
    next_review_date DATE         DEFAULT CURRENT_DATE,
    created_at       TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_flashcards_user_due ON flashcards(user_id, next_review_date);