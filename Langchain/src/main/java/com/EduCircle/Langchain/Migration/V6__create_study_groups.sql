-- V6__create_study_groups.sql
CREATE TABLE IF NOT EXISTS study_groups (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    owner_id    BIGINT       NOT NULL REFERENCES users(id),
    invite_code VARCHAR(12)  UNIQUE NOT NULL,
    created_at  TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS study_group_members (
    group_id  BIGINT NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    joined_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS study_group_notes (
    group_id  BIGINT NOT NULL REFERENCES study_groups(id) ON DELETE CASCADE,
    note_id   BIGINT NOT NULL REFERENCES notes(id) ON DELETE CASCADE,
    shared_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (group_id, note_id)
);