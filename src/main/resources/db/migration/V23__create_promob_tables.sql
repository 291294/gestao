-- =============================================
-- Promob Integration Module
-- =============================================

-- Tabela de projetos importados do Promob
CREATE TABLE promob_projects (
    id              BIGSERIAL PRIMARY KEY,
    company_id      BIGINT       NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    client_name     VARCHAR(255),
    client_id       BIGINT       REFERENCES clients(id),
    order_id        BIGINT       REFERENCES orders(id),
    environment     VARCHAR(255),
    designer        VARCHAR(255),
    total_value     NUMERIC(15,2),
    status          VARCHAR(50)  NOT NULL DEFAULT 'IMPORTED',
    notes           TEXT,
    imported_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Itens do projeto Promob (móveis/ambientes)
CREATE TABLE promob_project_items (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT       NOT NULL REFERENCES promob_projects(id) ON DELETE CASCADE,
    product_id      BIGINT       REFERENCES products(id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    quantity         INTEGER      NOT NULL DEFAULT 1,
    unit_price      NUMERIC(15,2),
    total_price     NUMERIC(15,2),
    width           NUMERIC(10,2),
    height          NUMERIC(10,2),
    depth           NUMERIC(10,2),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Lista de corte (cut list) do Promob
CREATE TABLE promob_cutlist (
    id              BIGSERIAL PRIMARY KEY,
    project_id      BIGINT       NOT NULL REFERENCES promob_projects(id) ON DELETE CASCADE,
    production_order_id BIGINT,
    part_name       VARCHAR(255) NOT NULL,
    material        VARCHAR(255),
    thickness       NUMERIC(10,2),
    width           NUMERIC(10,2),
    height          NUMERIC(10,2),
    quantity        INTEGER      NOT NULL DEFAULT 1,
    edge_top        BOOLEAN      DEFAULT FALSE,
    edge_bottom     BOOLEAN      DEFAULT FALSE,
    edge_left       BOOLEAN      DEFAULT FALSE,
    edge_right      BOOLEAN      DEFAULT FALSE,
    notes           VARCHAR(500),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_promob_projects_company ON promob_projects(company_id);
CREATE INDEX idx_promob_projects_status ON promob_projects(status);
CREATE INDEX idx_promob_items_project ON promob_project_items(project_id);
CREATE INDEX idx_promob_cutlist_project ON promob_cutlist(project_id);
