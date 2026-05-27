-- Make password nullable (OTP-based auth, no passwords)
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;

CREATE TABLE otp_tokens (
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    code       VARCHAR(255) NOT NULL,
    attempts   INT          NOT NULL DEFAULT 0,
    used       BOOLEAN      NOT NULL DEFAULT false,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_otp_tokens_email ON otp_tokens(email);
