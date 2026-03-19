-- V1__create_users.sql
CREATE TABLE IF NOT EXISTS users (
    id                  BIGSERIAL PRIMARY KEY,
    email               VARCHAR(255)  NOT NULL UNIQUE,
    password_hash       VARCHAR(255)  NOT NULL,
    name                VARCHAR(100)  NOT NULL,
    role                VARCHAR(20)   NOT NULL DEFAULT 'STUDENT'
                        CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN')),
    preferred_language  VARCHAR(30)   DEFAULT 'english',
    study_streak        INT           DEFAULT 0,
    last_active         DATE,
    is_active           BOOLEAN       DEFAULT TRUE,
    created_at          TIMESTAMPTZ   DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);