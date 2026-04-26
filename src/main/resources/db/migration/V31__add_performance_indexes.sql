-- ============================================================
-- V31 - Performance: índices críticos para consultas multi-tenant
-- Apenas índices ausentes — os já criados em V5/V6/V11/V25 foram mantidos
-- ============================================================

-- ------------------------------------------------------------
-- ORDERS
-- Queries mais frequentes: filtrar por company_id, status, período
-- idx_orders_client e idx_orders_status já existem desde V6
-- idx_orders_created_at já existe desde V6
-- ------------------------------------------------------------
ALTER TABLE orders ADD COLUMN IF NOT EXISTS company_id BIGINT;
UPDATE orders SET company_id = 1 WHERE company_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_orders_company_id     ON orders(company_id);
CREATE INDEX IF NOT EXISTS idx_orders_company_status ON orders(company_id, status);
CREATE INDEX IF NOT EXISTS idx_orders_company_date   ON orders(company_id, created_at DESC);

-- ------------------------------------------------------------
-- CLIENTS
-- idx_clients_company_id já existe desde V25 — nenhum índice novo necessário
-- Adicionar compound útil para busca por nome dentro da empresa
-- ------------------------------------------------------------
-- (nenhum índice novo — já coberto por V25)

-- ------------------------------------------------------------
-- PRODUCTS
-- idx_products_company_id já existe desde V25
-- Adicionar compound para busca paginada por empresa
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_products_company_name ON products(company_id, name);

-- ------------------------------------------------------------
-- INVENTORY_ITEMS
-- idx_inventory_company e idx_inventory_product já existem desde V11
-- Adicionar índice para queries de estoque baixo (hotpath de alertas)
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_inventory_low_stock
    ON inventory_items(company_id, quantity_on_hand, min_stock_level)
    WHERE (quantity_on_hand - quantity_reserved) <= min_stock_level;

-- ------------------------------------------------------------
-- AUDIT_LOGS — consultas por empresa + período são frequentes
-- ------------------------------------------------------------
CREATE INDEX IF NOT EXISTS idx_audit_logs_company_date
    ON audit_logs(company_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_type
    ON audit_logs(company_id, entity_type, entity_id);

-- ============================================================
-- Validação pós-deploy (rodar com \timing on no psql):
--
-- EXPLAIN ANALYZE
--   SELECT * FROM orders WHERE company_id = 1 AND status = 'PENDING'
--   ORDER BY created_at DESC LIMIT 20;
--
-- EXPLAIN ANALYZE
--   SELECT * FROM inventory_items
--   WHERE company_id = 1 AND (quantity_on_hand - quantity_reserved) <= min_stock_level;
--
-- Resultado esperado: "Index Scan" em vez de "Seq Scan"
-- ============================================================
