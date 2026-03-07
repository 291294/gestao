-- V7: Criar tabela de metas de vendas

CREATE TABLE sales_targets (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    seller_id BIGINT,
    target_type VARCHAR(20) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    target_amount DECIMAL(15,2) NOT NULL,
    achieved_amount DECIMAL(15,2) DEFAULT 0,
    achievement_percentage DECIMAL(5,2) DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_sales_target_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_sales_target_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT chk_sales_target_type CHECK (target_type IN ('INDIVIDUAL', 'TEAM', 'COMPANY')),
    CONSTRAINT chk_sales_target_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_sales_target_dates CHECK (period_end >= period_start),
    CONSTRAINT chk_sales_target_amount CHECK (target_amount > 0)
);

CREATE INDEX idx_sales_targets_seller ON sales_targets(seller_id);
CREATE INDEX idx_sales_targets_period ON sales_targets(period_start, period_end);
CREATE INDEX idx_sales_targets_status ON sales_targets(status);

COMMENT ON TABLE sales_targets IS 'Metas de vendas por vendedor/equipe/empresa';
COMMENT ON COLUMN sales_targets.target_type IS 'INDIVIDUAL=Vendedor específico, TEAM=Equipe, COMPANY=Empresa toda';
