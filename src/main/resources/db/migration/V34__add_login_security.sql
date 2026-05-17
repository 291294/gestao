-- ============================================================
-- V34 - Segurança de login: contador de falhas e bloqueio temporário
-- ============================================================

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS failed_login_attempts INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS locked_until          TIMESTAMP;

COMMENT ON COLUMN users.failed_login_attempts IS 'Contador de tentativas de login falhas consecutivas';
COMMENT ON COLUMN users.locked_until          IS 'Conta bloqueada até esta data/hora (NULL = não bloqueada)';
