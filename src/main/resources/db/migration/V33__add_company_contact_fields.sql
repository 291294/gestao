-- V33: Adiciona campos de contato à tabela companies
-- Necessário para CompanySettings: phone, email, address

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS phone VARCHAR(30),
    ADD COLUMN IF NOT EXISTS email VARCHAR(255),
    ADD COLUMN IF NOT EXISTS address VARCHAR(500);
