-- =====================================================
-- V12: Movimentações de Estoque
-- =====================================================
CREATE TABLE inventory_movements (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL REFERENCES companies(id),
    inventory_item_id   BIGINT          NOT NULL REFERENCES inventory_items(id),
    movement_type       VARCHAR(20)     NOT NULL,   -- IN, OUT, ADJUSTMENT, RETURN
    quantity            INTEGER         NOT NULL,
    previous_quantity   INTEGER         NOT NULL,
    new_quantity        INTEGER         NOT NULL,
    reference_type      VARCHAR(50),                -- ORDER, DELIVERY, ADJUSTMENT, RETURN
    reference_id        BIGINT,
    notes               TEXT,
    created_by          VARCHAR(100),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inv_mov_item      ON inventory_movements(inventory_item_id);
CREATE INDEX idx_inv_mov_type      ON inventory_movements(movement_type);
CREATE INDEX idx_inv_mov_ref       ON inventory_movements(reference_type, reference_id);
