-- ===========================================================================
-- V3: Create Default Admin User
-- ===========================================================================
-- Default password: admin123 (MUST be changed on first login)
-- Password hash generated with BCrypt (strength 10)

-- Insert default company
INSERT INTO companies (name, cnpj, active) VALUES
('Empresa Demo', '00.000.000/0001-00', true);

-- Insert admin user
-- Username: admin
-- Password: admin123 (BCrypt hash)
INSERT INTO users (username, email, password_hash, full_name, company_id, active, created_at)
VALUES (
    'admin',
    'admin@erp-moveis.com',
    '$2b$12$QW.Z/Bf5dt/oQod35NgDYe1fYQYW8MaIJxrwZDxKavepLxwrp1xA.', -- admin123
    'Administrador do Sistema',
    (SELECT id FROM companies WHERE cnpj = '00.000.000/0001-00'),
    true,
    NOW()
);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id, assigned_at)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'),
    (SELECT id FROM roles WHERE name = 'ADMIN'),
    NOW()
);

-- Log the admin creation in audit log
INSERT INTO audit_logs (user_id, action, entity_type, entity_id, new_value, created_at)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'),
    'CREATE_ADMIN_USER',
    'User',
    (SELECT id FROM users WHERE username = 'admin'),
    '{"username":"admin","role":"ADMIN","status":"initial_setup"}',
    NOW()
);

-- Add comment
COMMENT ON TABLE users IS 'Default admin user created. Username: admin, Password: admin123 (CHANGE ON FIRST LOGIN!)';
