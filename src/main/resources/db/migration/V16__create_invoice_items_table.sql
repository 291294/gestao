-- =====================================================
-- V16: Itens da Nota Fiscal
-- =====================================================
CREATE TABLE invoice_items (
    id                  BIGSERIAL       PRIMARY KEY,
    invoice_id          BIGINT          NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    product_id          BIGINT          NOT NULL REFERENCES products(id),
    description         VARCHAR(500),
    quantity            INTEGER         NOT NULL,
    unit_price          DECIMAL(15,2)   NOT NULL,
    discount_amount     DECIMAL(15,2)   DEFAULT 0,
    tax_amount          DECIMAL(15,2)   DEFAULT 0,
    subtotal            DECIMAL(15,2)   NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inv_item_invoice ON invoice_items(invoice_id);
