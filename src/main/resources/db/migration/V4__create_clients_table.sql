-- V4: Criar tabela de clientes

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    email VARCHAR(255),
    profession VARCHAR(100),
    preferences TEXT,
    created_at BIGINT,
    
    CONSTRAINT chk_clients_name CHECK (LENGTH(name) > 0)
);

CREATE INDEX idx_clients_name ON clients(name);
CREATE INDEX idx_clients_email ON clients(email);

COMMENT ON TABLE clients IS 'Clientes do sistema';
