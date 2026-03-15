-- =================================================================
-- V24: Tabela de Audit Logs (Spring AOP)
-- =================================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT,
    user_id         BIGINT,
    username        VARCHAR(100),
    action          VARCHAR(50)     NOT NULL,
    entity_type     VARCHAR(100)    NOT NULL,
    entity_id       BIGINT,
    details         TEXT,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_company ON audit_logs(company_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
