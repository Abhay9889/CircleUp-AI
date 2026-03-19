-- V7__create_notifications.sql
CREATE TABLE IF NOT EXISTS notifications (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type       VARCHAR(50)  NOT NULL
               CHECK (type IN ('FLASHCARD_DUE','STREAK_ALERT','WEEKLY_REPORT','NOTE_PROCESSED','GROUP_INVITE')),
    title      VARCHAR(255),
    message    TEXT,
    is_read    BOOLEAN      DEFAULT FALSE,
    created_at TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_notif_user_unread ON notifications(user_id, is_read, created_at DESC);