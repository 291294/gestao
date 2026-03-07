-- =====================================================
-- V14: Itens de Entrega
-- =====================================================
CREATE TABLE delivery_items (
    id              BIGSERIAL       PRIMARY KEY,
    delivery_id     BIGINT          NOT NULL REFERENCES deliveries(id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    quantity        INTEGER         NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_del_item_delivery ON delivery_items(delivery_id);
