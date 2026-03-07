-- =====================================================
-- V17: Pagamentos
-- =====================================================
CREATE TABLE payments (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL REFERENCES companies(id),
    invoice_id          BIGINT          NOT NULL REFERENCES invoices(id),
    payment_number      VARCHAR(50)     NOT NULL UNIQUE,
    amount              DECIMAL(15,2)   NOT NULL,
    payment_method      VARCHAR(30)     NOT NULL,   -- CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, PIX, BOLETO, CHECK
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    payment_date        TIMESTAMP,
    confirmation_date   TIMESTAMP,
    transaction_id      VARCHAR(200),
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX idx_payment_invoice   ON payments(invoice_id);
CREATE INDEX idx_payment_status    ON payments(status);
CREATE INDEX idx_payment_method    ON payments(payment_method);
CREATE INDEX idx_payment_company   ON payments(company_id);
