CREATE TABLE bill_of_materials (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bill_of_material_items (
    id BIGSERIAL PRIMARY KEY,
    bom_id BIGINT NOT NULL REFERENCES bill_of_materials(id),
    material_product_id BIGINT NOT NULL,
    quantity NUMERIC(15,2) NOT NULL
);

CREATE TABLE production_orders (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity NUMERIC(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bom_company ON bill_of_materials(company_id);
CREATE INDEX idx_bom_product ON bill_of_materials(product_id);
CREATE INDEX idx_production_order_company ON production_orders(company_id);
