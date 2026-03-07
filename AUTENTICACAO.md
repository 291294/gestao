# Sistema de Autenticação JWT + RBAC

## 📋 Visão Geral

Sistema completo de autenticação e autorização usando **JWT (JSON Web Tokens)** e **RBAC (Role-Based Access Control)** para o ERP de Móveis.

## 🏗️ Arquitetura de Segurança

### Camadas Implementadas

1. **Entities (JPA)** - Modelo de dados
   - `Company` - Suporte multiempresa
   - `User` - Implementa Spring Security UserDetails
   - `Role` - Papéis do sistema (ADMIN, GERENTE, VENDEDOR, etc.)
   - `Permission` - Permissões granulares (resource.action)

2. **Repositories (Spring Data JPA)**
   - `UserRepository` - Consultas de usuários
   - `RoleRepository` - Gestão de papéis
   - `PermissionRepository` - Gestão de permissões
   - `CompanyRepository` - Gestão de empresas

3. **Security Layer**
   - `JwtService` - Geração e validação de tokens
   - `JwtAuthenticationFilter` - Intercepta requests e valida JWT
   - `CustomUserDetailsService` - Carrega usuários do banco
   - `SecurityConfig` - Configuração Spring Security

4. **Business Layer**
   - `AuthService` - Lógica de autenticação e registro

5. **API Layer**
   - `AuthController` - Endpoints REST (`/auth/login`, `/auth/register`, `/auth/refresh`)

## 🗄️ Estrutura do Banco de Dados

### Tabelas Criadas (Flyway Migrations)

```sql
-- V1: Estrutura de segurança
companies (id, name, cnpj, active, created_at, updated_at)
users (id, username, email, password_hash, full_name, company_id, active, created_at, updated_at, created_by)
roles (id, name, description, company_id, active, created_at, updated_at)
permissions (id, resource, action, description, created_at)
user_roles (user_id, role_id, assigned_at, assigned_by)
role_permissions (role_id, permission_id)
audit_logs (id, user_id, action, entity_type, entity_id, old_value, new_value, ip_address, created_at)

-- V2: Dados iniciais (6 roles, 40+ permissions)
ADMIN - Acesso total ao sistema
GERENTE - Gestão e aprovações
VENDEDOR - Clientes e pedidos
FINANCEIRO - Pagamentos e notas fiscais
LOGISTICA - Entregas
PRODUCAO - Relatórios de produção

-- V3: Usuário admin padrão
Username: admin
Password: admin123
```

## 🔑 Permissões Granulares

O sistema usa o padrão `resource.action`:

### Recursos Disponíveis
- **client** - create, view, update, delete, list
- **product** - create, view, update, delete, list
- **project** - create, view, update, delete, list
- **order** - create, view, update, delete, list, approve, cancel
- **payment** - create, view, update, delete, list, approve
- **invoice** - create, view, update, delete, list, generate
- **delivery** - create, view, update, delete, list, schedule, complete
- **user** - create, view, update, delete, list, assign_role
- **report** - sales, financial, inventory, production

## 🚀 Como Executar

### 1. Executar Migrations (Flyway)

```bash
mvn flyway:migrate
```

Ou simplesmente rode a aplicação - Flyway executará automaticamente:

```bash
mvn spring-boot:run
```

### 2. Testar Autenticação

#### Login com usuário admin

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Resposta esperada:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin",
  "email": "admin@erp-moveis.com",
  "fullName": "Administrador"
}
```

#### Registrar novo usuário

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "joao.silva",
    "email": "joao.silva@example.com",
    "password": "senha123",
    "fullName": "João Silva",
    "companyId": 1
  }'
```

#### Acessar endpoint protegido

```bash
curl -X GET http://localhost:8080/api/clients \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"
```

#### Refresh token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Authorization: Bearer SEU_REFRESH_TOKEN_AQUI"
```

## 🔒 Segurança Implementada

### JWT Configuration
- **Secret Key**: 256-bit HMAC-SHA256
- **Access Token**: 24 horas (86400000 ms)
- **Refresh Token**: 7 dias (604800000 ms)

### Password Encoding
- **Algorithm**: BCrypt
- **Strength**: 10 rounds (default)

### Session Management
- **Strategy**: Stateless (sem sessão no servidor)
- **CSRF**: Desabilitado (JWT é stateless)

### Endpoints Públicos
- `/auth/**` - Autenticação (login, register, refresh)
- `/swagger-ui/**` - Documentação da API
- `/v3/api-docs/**` - OpenAPI specification
- `/actuator/health` - Health check

## 📊 Próximos Passos

### 1. Proteção por Permissões

Adicione `@PreAuthorize` nos métodos do controller:

```java
@PreAuthorize("hasAuthority('client.create')")
@PostMapping
public ResponseEntity<ClientDTO> createClient(@RequestBody ClientDTO dto) {
    // ...
}

@PreAuthorize("hasAuthority('order.approve')")
@PutMapping("/{id}/approve")
public ResponseEntity<Void> approveOrder(@PathVariable Long id) {
    // ...
}
```

### 2. Auditoria Automática

Implemente `AuditListener` para registrar alterações:

```java
@EntityListeners(AuditListener.class)
@Entity
public class Order {
    // ...
}
```

### 3. Gestão de Usuários

Crie endpoints para:
- Listar usuários por empresa
- Atribuir/remover roles
- Resetar senhas
- Ativar/desativar usuários

### 4. Dashboards por Role

- **ADMIN**: Visão completa do sistema
- **GERENTE**: Aprovações pendentes, relatórios consolidados
- **VENDEDOR**: Pipeline de vendas, clientes
- **FINANCEIRO**: Contas a pagar/receber, fluxo de caixa

## 🛡️ Boas Práticas de Segurança

✅ **Implementado:**
- Passwords hasheados com BCrypt
- JWT stateless
- RBAC granular (resource.action)
- Suporte multiempresa isolado
- Auditoria de ações

⚠️ **Recomendações:**
1. **Trocar o jwt.secret** em produção (use variável de ambiente)
2. **Trocar senha do admin** no primeiro login
3. **Implementar rate limiting** para /auth/login
4. **Adicionar refresh token rotation** para maior segurança
5. **Logs de auditoria** em todas as ações sensíveis
6. **Validar CNPJ** no cadastro de empresas

## 📚 Tecnologias Utilizadas

- **Spring Security 6.2.4** - Framework de segurança
- **JWT (jjwt 0.12.5)** - Tokens de autenticação
- **Flyway** - Database migrations
- **Spring Data JPA** - Persistência
- **PostgreSQL 42.7.3** - Banco de dados
- **BCrypt** - Hash de senhas
- **Jakarta Validation** - Validação de DTOs

## 🎯 Decisões de Design

### Por que JWT?
- Stateless (escalável horizontalmente)
- Auto-contido (todas as claims no token)
- Standard da indústria (RFC 7519)

### Por que RBAC?
- Flexível (permissões granulares)
- Manutenível (separação role/permission)
- Escalável (novos recursos facilmente adicionados)

### Por que Flyway?
- Versionamento de schema
- Repeatable migrations
- Controle total sobre estrutura do banco

## 🐛 Troubleshooting

### Erro: "Invalid JWT token"
- Verifique se o token não expirou
- Confirme que o `jwt.secret` é o mesmo usado na geração

### Erro: "User not found"
- Confirme que Flyway executou V3 (usuário admin)
- Verifique conexão com PostgreSQL

### Erro: "Access Denied"
- Verifique se o usuário tem a permissão necessária
- Confirme que o role foi atribuído corretamente

---

**Desenvolvido para ERP Móveis - Sistema Profissional de Gestão**
