-- V2__create_notes.sql
CREATE TABLE IF NOT EXISTS notes (
    id                  BIGSERIAL    PRIMARY KEY,
    user_id             BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title               VARCHAR(500) NOT NULL,
    file_key            VARCHAR(500),
    file_type           VARCHAR(20),
    file_size_bytes     BIGINT,
    vector_index_id     VARCHAR(100),
    summary             TEXT,
    difficulty_score    DECIMAL(5,2),
    tags                TEXT[],
    language            VARCHAR(30)  DEFAULT 'english',
    processing_status   VARCHAR(20)  DEFAULT 'PENDING'
                        CHECK (processing_status IN ('PENDING','PROCESSING','READY','FAILED')),
    uploaded_at         TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_notes_user_id   ON notes(user_id);
CREATE INDEX idx_notes_status    ON notes(user_id, processing_status);
CREATE INDEX idx_notes_tags      ON notes USING GIN(tags);