# 🏢 ERP Móveis - Sistema de Gestão Empresarial

<div align="center">

```
╔══════════════════════════════════════════════════════════╗
║                    ERP MÓVEIS                            ║
║                                                          ║
║   CRM  │  Vendas  │  Produção  │  Logística  │  Financeiro  ║
║                                                          ║
║         Sistema de Gestão Empresarial Completo           ║
╚══════════════════════════════════════════════════════════╝
```

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)]()

</div>

Sistema completo de gestão empresarial (ERP) desenvolvido para a indústria de móveis, com módulos integrados de CRM, Vendas, Produção, Logística e Financeiro.

## 🔄 Fluxo do Sistema

```
Cliente → Orçamento → Pedido → Produção → Entrega → Financeiro
   │          │          │         │          │          │
   └── CRM    └── Sales  └── Sales └── MFG    └── LOG    └── FIN
```

## 🎯 Funcionalidades

### ✅ Implementado
- **Autenticação JWT** - Tokens stateless com refresh automático
- **RBAC (Role-Based Access Control)** - Permissões granulares (resource.action)
- **Multiempresa** - Suporte para múltiplas empresas no mesmo sistema
- **Gestão de Clientes** - CRUD completo
- **Gestão de Produtos** - Cadastro e controle de estoque
- **Gestão de Projetos** - Acompanhamento de projetos customizados
- **Gestão de Pedidos** - Controle de vendas
- **Orçamentos** - Quotes com itens, aprovação e conversão em pedido
- **Comissões** - Cálculo automático de comissões de vendedores
- **Metas de Vendas** - Targets individuais, equipe e empresa
- **API REST** - Endpoints documentados com Swagger/OpenAPI
- **Database Migrations** - Flyway para versionamento de schema (V1-V10)
- **Auditoria** - Logs de alterações (estrutura preparada)

### 🔄 Em Desenvolvimento
- Módulo Financeiro (Contas a Pagar/Receber)
- Módulo de Logística (Entregas)
- Módulo de Produção (Ordens de Fabricação)
- Relatórios e Dashboards
- Gestão de Usuários (UI)

## 🗺️ Roadmap

| Status | Módulo | Descrição |
|--------|--------|-----------|
| ✅ | Security | JWT + RBAC + Multiempresa |
| ✅ | CRM | Gestão de Clientes |
| ✅ | Catalog | Produtos e Estoque |
| ✅ | Orders | Pedidos de Venda |
| ✅ | Quotes | Orçamentos |
| ✅ | Commissions | Comissões de Vendedores |
| ✅ | Sales Targets | Metas de Vendas |
| ⬜ | Finance | Contas a Pagar/Receber |
| ⬜ | Production | Ordens de Fabricação |
| ⬜ | Logistics | Entregas e Expedição |
| ⬜ | Dashboard | Relatórios e KPIs |
| ⬜ | Notifications | Alertas e Notificações |

## 🏗️ Arquitetura

### Stack Tecnológico
- **Backend**: Java 21 LTS, Spring Boot 3.2.5
- **Security**: Spring Security 6.2.4, JWT (jjwt 0.12.5)
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA, Hibernate 6.4.x
- **Migrations**: Flyway 10.10.0
- **API Docs**: Swagger/OpenAPI 2.5.0
- **Build**: Maven 3.9.6

### Infraestrutura Futura
- **Cache**: Redis
- **Mensageria**: RabbitMQ
- **Containers**: Docker / Docker Compose
- **CI/CD**: GitHub Actions
- **Monitoramento**: Spring Actuator + Prometheus + Grafana

### Padrões de Design
- **DDD-Lite** - Estrutura modular por domínio
- **Repository Pattern** - Abstração de acesso a dados
- **DTO Pattern** - Transferência de dados entre camadas
- **Builder Pattern** - Construção de objetos complexos
- **Strategy Pattern** - Autenticação e autorização

### Estrutura de Diretórios

```
src/main/java/com/erp/moveis/
│
├── core/                    # Núcleo do sistema
│   ├── auth/                # Autenticação (login, JWT, refresh)
│   ├── user/                # Usuários (entity, repository, service)
│   ├── company/             # Empresas (multiempresa)
│   ├── role/                # Papéis (ADMIN, VENDEDOR, etc.)
│   ├── permission/          # Permissões granulares
│   ├── security/            # Filtros, configuração Spring Security
│   └── config/              # Configurações gerais (DataLoader, etc.)
│
├── model/                   # Entidades de negócio
│   ├── Client.java          # Clientes
│   ├── Product.java         # Produtos
│   ├── Order.java           # Pedidos
│   └── Project.java         # Projetos customizados
│
├── sales/                   # Módulo de Vendas
│   ├── model/               # Quote, QuoteItem, Commission, SalesTarget
│   ├── dto/                 # Request/Response DTOs
│   └── repository/          # Repositórios JPA
│
├── controller/              # REST Controllers
├── service/                 # Serviços de negócio
├── repository/              # Repositórios base
├── dto/                     # DTOs compartilhados
└── config/                  # Configurações (Swagger, CORS, etc.)
```

## 🚀 Início Rápido

### Pré-requisitos
- JDK 21 ou superior
- Maven 3.9+
- PostgreSQL 12+

### 1. Clone o Repositório
```bash
git clone <repository-url>
cd erp-moveis
```

### 2. Configure o Banco de Dados
```sql
CREATE DATABASE erp_moveis;
```

Edite `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/erp_moveis
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

### 3. Compile e Execute
```bash
mvn spring-boot:run
```

> O Flyway executa automaticamente todas as migrations (V1-V10) na inicialização.

### 4. Acesse a API
- **API Base**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **Health Check**: http://localhost:8080/api/actuator/health

## 🔐 Autenticação

### Login Padrão
```
Username: admin
Password: admin123
```

⚠️ **IMPORTANTE**: Altere a senha padrão em produção!

### Exemplo de Login via API
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Resposta:**
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

### Usando o Token
```bash
curl -X GET http://localhost:8080/api/clients \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

## 👥 Roles e Permissões

### Roles Padrão
| Role | Descrição | Permissões |
|------|-----------|------------|
| **ADMIN** | Administrador do sistema | Todas as permissões |
| **GERENTE** | Gerente | Aprovações, relatórios, gestão |
| **VENDEDOR** | Vendedor | Clientes, pedidos (CRUD) |
| **FINANCEIRO** | Financeiro | Pagamentos, notas fiscais |
| **LOGISTICA** | Logística | Entregas, expedição |
| **PRODUCAO** | Produção | Relatórios de produção |

### Permissões Granulares (resource.action)
```
client.{create, read, update, delete, list}
product.{create, read, update, delete, list}
project.{create, read, update, delete, list}
order.{create, read, update, delete, list, approve, cancel}
quote.{create, read, update, delete, list, approve, convert}
payment.{create, read, update, delete, list, approve}
invoice.{create, read, update, delete, list, generate}
delivery.{create, read, update, delete, list, schedule, complete}
user.{create, read, update, delete, list, assign_role}
report.{sales, financial, inventory, production}
```

## 📚 Documentação Adicional

- [**Guia de Autenticação**](AUTENTICACAO.md) - Detalhes sobre JWT + RBAC
- [**Histórico de Upgrade**](.github/java-upgrade/20260307010415/) - Java 11→21, Spring Boot 3.0→3.2

## 🗄️ Estrutura do Banco de Dados

### Módulo de Segurança
- `companies` - Empresas (multiempresa)
- `users` - Usuários do sistema
- `roles` - Papéis (ADMIN, VENDEDOR, etc.)
- `permissions` - Permissões granulares
- `user_roles` - Relacionamento usuário-papel
- `role_permissions` - Relacionamento papel-permissão
- `audit_logs` - Auditoria de ações

### Módulo de Negócio
- `clients` - Clientes
- `products` - Produtos
- `orders` - Pedidos de venda

### Módulo de Vendas
- `quotes` - Orçamentos
- `quote_items` - Itens do orçamento
- `commissions` - Comissões de vendedores
- `sales_targets` - Metas de vendas

## 🔧 Comandos Úteis

### Compilar
```bash
mvn clean compile
```

### Executar Testes
```bash
mvn test
```

### Gerar JAR
```bash
mvn clean package
```

### Executar JAR
```bash
java -jar target/erp-moveis-1.0-SNAPSHOT.jar
```

### Resetar Database
```bash
# Execute clean_database.sql no pgAdmin, depois:
mvn spring-boot:run
```

## 📊 Métricas de Qualidade

- ✅ **Compilação**: 100% sucesso
- ✅ **Vulnerabilidades**: 0 CVEs detectados
- ✅ **Endpoints**: 16 (13 protegidos + 3 públicos)
- ✅ **Migrations**: 10 (V1-V10)
- ✅ **Tabelas**: 14
- ✅ **Cobertura de Testes**: Em desenvolvimento

## 🛡️ Segurança

### Implementações
- ✅ Passwords hasheados com BCrypt (10 rounds)
- ✅ JWT stateless (sem sessão no servidor)
- ✅ CSRF desabilitado (stateless API)
- ✅ CORS configurável
- ✅ RBAC com permissões granulares
- ✅ Tokens com expiração (access: 24h, refresh: 7 dias)

### Recomendações para Produção
1. ⚠️ Trocar `jwt.secret` (use variável de ambiente)
2. ⚠️ Configurar HTTPS/TLS
3. ⚠️ Implementar rate limiting
4. ⚠️ Habilitar CORS específico (não usar "*")
5. ⚠️ Logs de auditoria em arquivo/serviço externo
6. ⚠️ Implementar refresh token rotation

## 🤝 Contribuindo

Este é um projeto proprietário. Contribuições são aceitas mediante acordo prévio.

## 📄 Licença

Proprietário - Todos os direitos reservados.

## 📞 Suporte

Para questões ou suporte, contate: contato@erp-moveis.com

---

**Desenvolvido com ❤️ para a indústria de móveis**
