-- V25: Multi-tenant SaaS - Add company_id to products, clients and projects
-- This migration adds company_id to tables that were missing tenant isolation

-- Add company_id to products table
ALTER TABLE products ADD COLUMN IF NOT EXISTS company_id BIGINT;
UPDATE products SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE products ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE products ADD CONSTRAINT fk_products_company FOREIGN KEY (company_id) REFERENCES companies(id);
CREATE INDEX IF NOT EXISTS idx_products_company_id ON products(company_id);

-- Add company_id to clients table
ALTER TABLE clients ADD COLUMN IF NOT EXISTS company_id BIGINT;
UPDATE clients SET company_id = 1 WHERE company_id IS NULL;
ALTER TABLE clients ALTER COLUMN company_id SET NOT NULL;
ALTER TABLE clients ADD CONSTRAINT fk_clients_company FOREIGN KEY (company_id) REFERENCES companies(id);
CREATE INDEX IF NOT EXISTS idx_clients_company_id ON clients(company_id);

-- Create projects table if it doesn't exist
CREATE TABLE IF NOT EXISTS projects (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    budget      DOUBLE PRECISION,
    client_id   BIGINT       REFERENCES clients(id),
    company_id  BIGINT       NOT NULL REFERENCES companies(id),
    created_at  BIGINT
);
CREATE INDEX IF NOT EXISTS idx_projects_company_id ON projects(company_id);
CREATE INDEX IF NOT EXISTS idx_projects_client_id ON projects(client_id);
