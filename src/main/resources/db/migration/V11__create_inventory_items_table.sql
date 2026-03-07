-- =====================================================
-- V11: Itens de Estoque
-- =====================================================
CREATE TABLE inventory_items (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    warehouse_location VARCHAR(100),
    quantity_on_hand   INTEGER      NOT NULL DEFAULT 0,
    quantity_reserved  INTEGER      NOT NULL DEFAULT 0,
    min_stock_level    INTEGER      DEFAULT 0,
    max_stock_level    INTEGER,
    unit_cost       DECIMAL(15,2),
    last_restock_date DATE,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP,
    UNIQUE(company_id, product_id)
);

CREATE INDEX idx_inventory_company   ON inventory_items(company_id);
CREATE INDEX idx_inventory_product   ON inventory_items(product_id);
