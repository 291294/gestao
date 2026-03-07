-- V5: Criar tabela de itens de orçamento

CREATE TABLE quote_items (
    id BIGSERIAL PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(15,2) NOT NULL,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    subtotal DECIMAL(15,2) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_quote_item_quote FOREIGN KEY (quote_id) REFERENCES quotes(id) ON DELETE CASCADE,
    CONSTRAINT fk_quote_item_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_quote_item_quantity CHECK (quantity > 0),
    CONSTRAINT chk_quote_item_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_quote_item_discount_pct CHECK (discount_percentage BETWEEN 0 AND 100)
);

CREATE INDEX idx_quote_items_quote ON quote_items(quote_id);
CREATE INDEX idx_quote_items_product ON quote_items(product_id);

COMMENT ON TABLE quote_items IS 'Itens do orçamento (produtos e quantidades)';
