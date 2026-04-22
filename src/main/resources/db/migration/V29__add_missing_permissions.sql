-- V29: Add missing permissions for all modules and assign to roles
-- Corrige: ADMIN sem permissões de audit, commission, inventory, quote, etc.

-- ── Audit permissions ──────────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('audit', 'list',  'Listar logs de auditoria'),
    ('audit', 'view',  'Visualizar log de auditoria')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Commission permissions ─────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('commission', 'create',  'Criar comissão'),
    ('commission', 'view',    'Visualizar comissão'),
    ('commission', 'list',    'Listar comissões'),
    ('commission', 'approve', 'Aprovar comissão'),
    ('commission', 'pay',     'Pagar comissão'),
    ('commission', 'cancel',  'Cancelar comissão')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Inventory permissions ──────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('inventory', 'create', 'Criar item de estoque'),
    ('inventory', 'view',   'Visualizar estoque'),
    ('inventory', 'update', 'Atualizar estoque'),
    ('inventory', 'delete', 'Remover item de estoque'),
    ('inventory', 'list',   'Listar estoque'),
    ('inventory', 'adjust', 'Ajustar estoque')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Warehouse permissions ──────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('warehouse', 'create', 'Criar armazém'),
    ('warehouse', 'view',   'Visualizar armazém'),
    ('warehouse', 'update', 'Atualizar armazém'),
    ('warehouse', 'delete', 'Remover armazém'),
    ('warehouse', 'list',   'Listar armazéns')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Manufacturing permissions ──────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('manufacturing', 'create', 'Criar ordem de produção'),
    ('manufacturing', 'view',   'Visualizar produção'),
    ('manufacturing', 'update', 'Atualizar produção'),
    ('manufacturing', 'list',   'Listar produção'),
    ('manufacturing', 'start',  'Iniciar produção'),
    ('manufacturing', 'finish', 'Finalizar produção')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Quote permissions ──────────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('quote', 'create',  'Criar orçamento'),
    ('quote', 'view',    'Visualizar orçamento'),
    ('quote', 'update',  'Editar orçamento'),
    ('quote', 'delete',  'Excluir orçamento'),
    ('quote', 'list',    'Listar orçamentos'),
    ('quote', 'approve', 'Aprovar orçamento'),
    ('quote', 'reject',  'Rejeitar orçamento'),
    ('quote', 'convert', 'Converter orçamento em pedido')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Sales Target permissions ───────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('sales_target', 'create',   'Criar meta de vendas'),
    ('sales_target', 'view',     'Visualizar meta'),
    ('sales_target', 'list',     'Listar metas'),
    ('sales_target', 'complete', 'Concluir meta'),
    ('sales_target', 'cancel',   'Cancelar meta')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Notification permissions ───────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('notification', 'list', 'Listar notificações'),
    ('notification', 'read', 'Marcar notificação como lida')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Supplier permissions ───────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('supplier', 'create', 'Criar fornecedor'),
    ('supplier', 'view',   'Visualizar fornecedor'),
    ('supplier', 'update', 'Editar fornecedor'),
    ('supplier', 'delete', 'Excluir fornecedor'),
    ('supplier', 'list',   'Listar fornecedores')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Analytics permissions ──────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('analytics', 'view',      'Visualizar analytics'),
    ('analytics', 'dashboard', 'Acessar dashboard')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Company permissions ────────────────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('company', 'view',   'Visualizar empresa'),
    ('company', 'update', 'Editar configurações da empresa')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Service Assistance permissions ────────────────────────────────────────
INSERT INTO permissions (resource, action, description)
VALUES
    ('service_assistance', 'create', 'Criar assistência de serviço'),
    ('service_assistance', 'view',   'Visualizar assistência'),
    ('service_assistance', 'update', 'Editar assistência'),
    ('service_assistance', 'delete', 'Excluir assistência'),
    ('service_assistance', 'list',   'Listar assistências')
ON CONFLICT (resource, action) DO NOTHING;

-- ── Assign ALL permissions to ADMIN ───────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    p.id
FROM permissions p
WHERE NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'ADMIN')
      AND rp.permission_id = p.id
);

-- ── GERENTE: acesso amplo (leitura + aprovação + relatórios) ──────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'GERENTE'),
    p.id
FROM permissions p
WHERE p.resource IN ('analytics', 'audit', 'commission', 'company', 'client',
                     'delivery', 'inventory', 'invoice', 'manufacturing',
                     'notification', 'order', 'payment', 'product', 'project',
                     'quote', 'report', 'sales_target', 'supplier', 'warehouse')
  AND p.action IN ('list', 'view', 'approve', 'dashboard', 'financial', 'sales', 'inventory', 'production')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'GERENTE')
      AND rp.permission_id = p.id
);

-- ── VENDEDOR: clientes, pedidos, orçamentos, comissões, notificações ──────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'VENDEDOR'),
    p.id
FROM permissions p
WHERE (
    (p.resource IN ('client', 'order', 'product', 'project', 'quote', 'notification')
      AND p.action IN ('create', 'view', 'list', 'update'))
   OR (p.resource = 'commission' AND p.action IN ('view', 'list'))
   OR (p.resource = 'sales_target' AND p.action IN ('view', 'list'))
   OR (p.resource = 'service_assistance' AND p.action IN ('create', 'view', 'list', 'update'))
)
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'VENDEDOR')
      AND rp.permission_id = p.id
);

-- ── FINANCEIRO: pagamentos, faturas, comissões, relatórios financeiros ────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'FINANCEIRO'),
    p.id
FROM permissions p
WHERE (
    (p.resource IN ('payment', 'invoice', 'commission', 'notification')
      AND p.action IN ('create', 'view', 'list', 'update', 'approve', 'cancel', 'pay'))
   OR (p.resource = 'report' AND p.action = 'financial')
   OR (p.resource = 'analytics' AND p.action IN ('view', 'dashboard'))
)
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'FINANCEIRO')
      AND rp.permission_id = p.id
);

-- ── LOGISTICA: entregas, estoque, armazéns ────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'LOGISTICA'),
    p.id
FROM permissions p
WHERE p.resource IN ('delivery', 'inventory', 'warehouse', 'notification')
  AND p.action IN ('create', 'view', 'list', 'update', 'schedule', 'complete', 'adjust')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'LOGISTICA')
      AND rp.permission_id = p.id
);

-- ── PRODUCAO: ordens de produção, estoque (leitura), notificações ─────────
INSERT INTO role_permissions (role_id, permission_id)
SELECT
    (SELECT id FROM roles WHERE name = 'PRODUCAO'),
    p.id
FROM permissions p
WHERE (
    (p.resource = 'manufacturing' AND p.action IN ('create', 'view', 'list', 'start', 'finish'))
   OR (p.resource = 'inventory' AND p.action IN ('view', 'list'))
   OR (p.resource = 'product' AND p.action IN ('view', 'list'))
   OR (p.resource = 'notification' AND p.action IN ('list', 'read'))
)
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = (SELECT id FROM roles WHERE name = 'PRODUCAO')
      AND rp.permission_id = p.id
);
