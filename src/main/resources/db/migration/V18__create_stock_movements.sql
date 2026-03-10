-- =====================================================
-- V18: Movimentos de Estoque (warehouse-aware)
-- =====================================================
CREATE TABLE stock_movements (
    id              BIGSERIAL       PRIMARY KEY,
    company_id      BIGINT          NOT NULL REFERENCES companies(id),
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    warehouse_id    BIGINT          NOT NULL,

    movement_type   VARCHAR(30)     NOT NULL,
    quantity        NUMERIC(15,2)   NOT NULL,

    reference_type  VARCHAR(50),
    reference_id    BIGINT,

    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_stock_mov_product   ON stock_movements(product_id);
CREATE INDEX idx_stock_mov_warehouse ON stock_movements(warehouse_id);
CREATE INDEX idx_stock_mov_company   ON stock_movements(company_id);
CREATE INDEX idx_stock_mov_type      ON stock_movements(movement_type);
