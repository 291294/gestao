-- ===========================================================================
-- V2: Insert Default Roles and Permissions
-- ===========================================================================

-- Insert default roles
INSERT INTO roles (name, description, company_id, active) VALUES
('ADMIN', 'Administrador do sistema - acesso total', NULL, true),
('GERENTE', 'Gerente - acesso a relatórios e aprovações', NULL, true),
('VENDEDOR', 'Vendedor - gestão de clientes e pedidos', NULL, true),
('FINANCEIRO', 'Financeiro - gestão de pagamentos e faturas', NULL, true),
('LOGISTICA', 'Logística - gestão de entregas e estoque', NULL, true),
('PRODUCAO', 'Produção - gestão de ordens de produção', NULL, true);

-- Insert default permissions

-- Client permissions
INSERT INTO permissions (resource, action, description) VALUES
('client', 'create', 'Criar cliente'),
('client', 'view', 'Visualizar cliente'),
('client', 'update', 'Editar cliente'),
('client', 'delete', 'Excluir cliente'),
('client', 'list', 'Listar clientes');

-- Product permissions
INSERT INTO permissions (resource, action, description) VALUES
('product', 'create', 'Criar produto'),
('product', 'view', 'Visualizar produto'),
('product', 'update', 'Editar produto'),
('product', 'delete', 'Excluir produto'),
('product', 'list', 'Listar produtos');

-- Project permissions
INSERT INTO permissions (resource, action, description) VALUES
('project', 'create', 'Criar projeto'),
('project', 'view', 'Visualizar projeto'),
('project', 'update', 'Editar projeto'),
('project', 'delete', 'Excluir projeto'),
('project', 'list', 'Listar projetos');

-- Order permissions
INSERT INTO permissions (resource, action, description) VALUES
('order', 'create', 'Criar pedido'),
('order', 'view', 'Visualizar pedido'),
('order', 'update', 'Editar pedido'),
('order', 'delete', 'Excluir pedido'),
('order', 'cancel', 'Cancelar pedido'),
('order', 'approve', 'Aprovar pedido'),
('order', 'list', 'Listar pedidos');

-- Payment permissions
INSERT INTO permissions (resource, action, description) VALUES
('payment', 'create', 'Registrar pagamento'),
('payment', 'view', 'Visualizar pagamento'),
('payment', 'update', 'Editar pagamento'),
('payment', 'approve', 'Aprovar pagamento'),
('payment', 'cancel', 'Cancelar pagamento'),
('payment', 'list', 'Listar pagamentos');

-- Invoice permissions
INSERT INTO permissions (resource, action, description) VALUES
('invoice', 'create', 'Criar fatura'),
('invoice', 'view', 'Visualizar fatura'),
('invoice', 'update', 'Editar fatura'),
('invoice', 'cancel', 'Cancelar fatura'),
('invoice', 'generate', 'Gerar nota fiscal'),
('invoice', 'list', 'Listar faturas');

-- Delivery permissions
INSERT INTO permissions (resource, action, description) VALUES
('delivery', 'create', 'Criar entrega'),
('delivery', 'view', 'Visualizar entrega'),
('delivery', 'update', 'Editar entrega'),
('delivery', 'schedule', 'Agendar entrega'),
('delivery', 'complete', 'Finalizar entrega'),
('delivery', 'list', 'Listar entregas');

-- User management permissions
INSERT INTO permissions (resource, action, description) VALUES
('user', 'create', 'Criar usuário'),
('user', 'view', 'Visualizar usuário'),
('user', 'update', 'Editar usuário'),
('user', 'delete', 'Excluir usuário'),
('user', 'list', 'Listar usuários'),
('user', 'assign_role', 'Atribuir papel a usuário');

-- Report permissions
INSERT INTO permissions (resource, action, description) VALUES
('report', 'sales', 'Visualizar relatório de vendas'),
('report', 'financial', 'Visualizar relatório financeiro'),
('report', 'inventory', 'Visualizar relatório de estoque'),
('report', 'production', 'Visualizar relatório de produção');

-- Assign permissions to ADMIN role (all permissions)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    id
FROM permissions;

-- Assign permissions to GERENTE role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'GERENTE'),
    id
FROM permissions
WHERE resource IN ('client', 'product', 'project', 'order', 'report')
   OR (resource = 'order' AND action = 'approve');

-- Assign permissions to VENDEDOR role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'VENDEDOR'),
    id
FROM permissions
WHERE resource IN ('client', 'product', 'project', 'order')
  AND action IN ('create', 'view', 'update', 'list');

-- Assign permissions to FINANCEIRO role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'FINANCEIRO'),
    id
FROM permissions
WHERE resource IN ('payment', 'invoice', 'order')
   OR (resource = 'report' AND action = 'financial');

-- Assign permissions to LOGISTICA role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'LOGISTICA'),
    id
FROM permissions
WHERE resource IN ('delivery', 'order')
   OR (resource = 'order' AND action IN ('view', 'list'));

-- Assign permissions to PRODUCAO role
INSERT INTO role_permissions (role_id, permission_id)
SELECT 
    (SELECT id FROM roles WHERE name = 'PRODUCAO'),
    id
FROM permissions
WHERE resource IN ('product', 'order', 'project')
   OR (resource = 'report' AND action = 'production');
