-- =====================================================
-- V13: Entregas
-- =====================================================
CREATE TABLE deliveries (
    id                  BIGSERIAL       PRIMARY KEY,
    company_id          BIGINT          NOT NULL REFERENCES companies(id),
    order_id            BIGINT          NOT NULL REFERENCES orders(id),
    delivery_number     VARCHAR(50)     NOT NULL UNIQUE,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    scheduled_date      DATE,
    shipped_date        TIMESTAMP,
    delivered_date      TIMESTAMP,
    delivery_address    TEXT,
    receiver_name       VARCHAR(200),
    driver_name         VARCHAR(200),
    notes               TEXT,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP
);

CREATE INDEX idx_delivery_order    ON deliveries(order_id);
CREATE INDEX idx_delivery_status   ON deliveries(status);
CREATE INDEX idx_delivery_company  ON deliveries(company_id);
