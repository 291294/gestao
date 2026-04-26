-- ============================================================
-- V32 - Auditoria enriquecida: captura de diff (antes/depois)
-- Adiciona request_data e response_data para rastrear "o que mudou"
-- ============================================================

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS request_data  TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS response_data TEXT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS ip_address    VARCHAR(45);

COMMENT ON COLUMN audit_logs.request_data  IS 'JSON dos dados enviados na requisição (DTO de entrada)';
COMMENT ON COLUMN audit_logs.response_data IS 'JSON do estado da entidade após a operação';
COMMENT ON COLUMN audit_logs.ip_address    IS 'IP de origem da requisição (suporta IPv6)';
