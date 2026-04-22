-- V28: Create service_assistances table
-- Módulo de Assistência de Serviço

CREATE TABLE IF NOT EXISTS service_assistances (
    id                    BIGSERIAL     PRIMARY KEY,
    company_id            BIGINT        NOT NULL REFERENCES companies(id),
    client_name           VARCHAR(255)  NOT NULL,
    client_address        VARCHAR(500)  NOT NULL,
    scheduled_date        DATE          NOT NULL,
    service_description   TEXT          NOT NULL,
    material_requested_at DATE,
    photo_url             VARCHAR(1000),
    notes                 TEXT,
    status                VARCHAR(30)   NOT NULL DEFAULT 'SCHEDULED',
    notification_sent     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_service_assistances_company_id   ON service_assistances(company_id);
CREATE INDEX IF NOT EXISTS idx_service_assistances_scheduled_date ON service_assistances(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_service_assistances_notification  ON service_assistances(scheduled_date, notification_sent, status);
