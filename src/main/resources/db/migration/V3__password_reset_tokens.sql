CREATE TABLE password_reset_tokens (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    token      VARCHAR(64)  NOT NULL UNIQUE,
    used       BOOLEAN      NOT NULL DEFAULT false,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
