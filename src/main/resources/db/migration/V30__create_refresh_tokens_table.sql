-- ============================================================
-- V30 - Refresh tokens persistidos no banco (JWT hardening)
-- Permite rotação, revogação e detecção de replay attack
-- ============================================================

CREATE TABLE refresh_tokens (
    id          BIGSERIAL     PRIMARY KEY,
    token       VARCHAR(36)   NOT NULL UNIQUE,
    user_id     BIGINT        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at  TIMESTAMP     NOT NULL,
    revoked     BOOLEAN       NOT NULL DEFAULT FALSE,
    replaced_by VARCHAR(36),
    created_at  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_refresh_token_token   ON refresh_tokens(token);
CREATE INDEX idx_refresh_token_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_revoked ON refresh_tokens(revoked) WHERE revoked = FALSE;
