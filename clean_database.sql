-- Script para limpar o banco de dados ERP e permitir reexecução das migrations
-- Execute este script no pgAdmin conectado ao banco erp_moveis

-- Dropar todas as tabelas na ordem correta (respeitando foreign keys)
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS quote_items CASCADE;
DROP TABLE IF EXISTS quotes CASCADE;
DROP TABLE IF EXISTS commissions CASCADE;
DROP TABLE IF EXISTS sales_targets CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS clients CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS companies CASCADE;

-- Mensagem de confirmação
SELECT 'Banco de dados limpo com sucesso! Execute mvn spring-boot:run para recriar todas as tabelas.' AS status;
