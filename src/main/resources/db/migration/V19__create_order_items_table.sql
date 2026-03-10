-- =====================================================
-- V19: Evolução do módulo de pedidos
-- Adicionar company_id + tabela order_items
-- =====================================================

-- Adicionar company_id na tabela orders
ALTER TABLE orders ADD COLUMN company_id BIGINT REFERENCES companies(id);

-- Atualizar orders existentes com a primeira empresa disponível
UPDATE orders SET company_id = (SELECT id FROM companies LIMIT 1) WHERE company_id IS NULL;

-- Tornar company_id NOT NULL após popular
ALTER TABLE orders ALTER COLUMN company_id SET NOT NULL;

CREATE INDEX idx_orders_company ON orders(company_id);

-- Tabela de itens do pedido
CREATE TABLE order_items (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id      BIGINT          NOT NULL REFERENCES products(id),
    quantity        NUMERIC(15,2)   NOT NULL,
    unit_price      NUMERIC(15,2)   NOT NULL,
    subtotal        NUMERIC(15,2)   NOT NULL,
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_order_items_order   ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);
