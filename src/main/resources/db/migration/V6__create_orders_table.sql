-- V6: Criar tabela de pedidos

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    total_value DOUBLE PRECISION,
    client_id BIGINT NOT NULL,
    status VARCHAR(50),
    created_at BIGINT,
    updated_at BIGINT,
    
    CONSTRAINT fk_orders_client FOREIGN KEY (client_id) REFERENCES clients(id),
    CONSTRAINT chk_orders_value CHECK (total_value IS NULL OR total_value >= 0)
);

CREATE INDEX idx_orders_client ON orders(client_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at);

COMMENT ON TABLE orders IS 'Pedidos de clientes';
