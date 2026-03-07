-- V4: Criar tabela de orçamentos (quotes)

CREATE TABLE quotes (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    quote_number VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    final_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    valid_until DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    
    CONSTRAINT fk_quote_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_quote_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT fk_quote_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT chk_quote_status CHECK (status IN ('DRAFT', 'SENT', 'APPROVED', 'REJECTED', 'EXPIRED', 'CONVERTED'))
);

CREATE INDEX idx_quotes_company ON quotes(company_id);
CREATE INDEX idx_quotes_client ON quotes(client_id);
CREATE INDEX idx_quotes_seller ON quotes(seller_id);
CREATE INDEX idx_quotes_status ON quotes(status);
CREATE INDEX idx_quotes_created_at ON quotes(created_at);

COMMENT ON TABLE quotes IS 'Orçamentos de vendas';
COMMENT ON COLUMN quotes.status IS 'DRAFT=Rascunho, SENT=Enviado, APPROVED=Aprovado, REJECTED=Rejeitado, EXPIRED=Expirado, CONVERTED=Convertido em pedido';
