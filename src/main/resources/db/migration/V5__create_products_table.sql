-- V5: Criar tabela de produtos

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    material VARCHAR(100),
    color VARCHAR(50),
    base_price DOUBLE PRECISION,
    created_at BIGINT,
    
    CONSTRAINT chk_products_name CHECK (LENGTH(name) > 0),
    CONSTRAINT chk_products_price CHECK (base_price IS NULL OR base_price >= 0)
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_material ON products(material);

COMMENT ON TABLE products IS 'Produtos (móveis) disponíveis';
