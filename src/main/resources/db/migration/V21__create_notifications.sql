CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_notifications_company
ON notifications(company_id);

CREATE INDEX idx_notifications_read
ON notifications(read);
