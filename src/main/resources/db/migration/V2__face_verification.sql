-- ============================================================
-- Face Verification: add column to users + verification tokens
-- ============================================================

ALTER TABLE users
    ADD COLUMN face_verified BOOLEAN NOT NULL DEFAULT false;

CREATE TABLE face_verification_tokens (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(36)  NOT NULL UNIQUE,
    used       BOOLEAN      NOT NULL DEFAULT false,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_face_verification_tokens_token ON face_verification_tokens(token);
