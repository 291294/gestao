-- =====================================================
-- V15: Notas Fiscais / Faturas
-- =====================================================
CREATE TABLE invoices (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL REFERENCES companies(id),
    client_id           BIGINT          NOT NULL REFERENCES clients(id),
    order_id            BIGINT          REFERENCES orders(id),
    delivery_id         BIGINT          REFERENCES deliveries(id),
    invoice_number      VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    issue_date          DATE,
    due_date            DATE,
    subtotal            DECIMAL(15,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(15,2)   DEFAULT 0,
    tax_amount          DECIMAL(15,2)   DEFAULT 0,
    total_amount        DECIMAL(15,2)   NOT NULL DEFAULT 0,
    amount_paid         DECIMAL(15,2)   DEFAULT 0,
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX idx_invoice_company   ON invoices(company_id);
CREATE INDEX idx_invoice_client    ON invoices(client_id);
CREATE INDEX idx_invoice_order     ON invoices(order_id);
CREATE INDEX idx_invoice_status    ON invoices(status);
CREATE INDEX idx_invoice_due       ON invoices(due_date);
