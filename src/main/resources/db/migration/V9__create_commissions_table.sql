-- V6: Criar tabela de comissões de vendas

CREATE TABLE commissions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    quote_id BIGINT,
    commission_percentage DECIMAL(5,2) NOT NULL,
    sale_amount DECIMAL(15,2) NOT NULL,
    commission_amount DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATE,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_commission_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_commission_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_commission_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_commission_quote FOREIGN KEY (quote_id) REFERENCES quotes(id),
    CONSTRAINT chk_commission_status CHECK (status IN ('PENDING', 'APPROVED', 'PAID', 'CANCELLED')),
    CONSTRAINT chk_commission_percentage CHECK (commission_percentage BETWEEN 0 AND 100),
    CONSTRAINT chk_commission_amounts CHECK (commission_amount = sale_amount * commission_percentage / 100)
);

CREATE INDEX idx_commissions_seller ON commissions(seller_id);
CREATE INDEX idx_commissions_order ON commissions(order_id);
CREATE INDEX idx_commissions_status ON commissions(status);
CREATE INDEX idx_commissions_payment_date ON commissions(payment_date);

COMMENT ON TABLE commissions IS 'Comissões de vendedores sobre pedidos realizados';
COMMENT ON COLUMN commissions.status IS 'PENDING=Pendente, APPROVED=Aprovada, PAID=Paga, CANCELLED=Cancelada';
