-- =================================================================
-- V24: Upgrade audit_logs table (originally created in V1)
--      Add missing columns for Spring AOP audit
-- =================================================================

-- Add new columns to existing audit_logs table
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS company_id BIGINT;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS username VARCHAR(100);
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS details TEXT;

-- Widen action column (V1 used VARCHAR(100), keep compatible)
-- No-op if already wide enough

-- Add missing indexes (IF NOT EXISTS)
CREATE INDEX IF NOT EXISTS idx_audit_logs_company ON audit_logs(company_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
