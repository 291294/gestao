# 🏢 ERP Móveis - Sistema de Gestão Empresarial

<div align="center">

```
╔══════════════════════════════════════════════════════════════╗
║                       ERP MÓVEIS                             ║
║                                                              ║
║  CRM │ Vendas │ Produção │ Estoque │ Logística │ Financeiro  ║
║                                                              ║
║          Sistema de Gestão Empresarial Completo              ║
╚══════════════════════════════════════════════════════════════╝
```

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![React](https://img.shields.io/badge/React-19-61dafb.svg)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-7.3-646cff.svg)](https://vite.dev/)
[![License](https://img.shields.io/badge/license-Proprietary-red.svg)]()

</div>

Sistema completo de gestão empresarial (ERP) desenvolvido para a indústria de móveis, com módulos integrados de CRM, Vendas, Produção, Estoque, Logística e Financeiro. Inclui backend API REST (Java/Spring Boot) e frontend SPA (React/MUI).

## 📦 Repositórios

| Repositório | Descrição |
|-------------|-----------|
| [`erp-moveis`](https://github.com/291294/gestao) (este) | Backend - API REST, Java 21, Spring Boot 3.2.5 |
| [`erp-frontend`](https://github.com/291294/erp-frontend) | Frontend - React 19, MUI 5, Vite 7 |

## 🔄 Fluxo do Sistema

```
Cliente → Orçamento → Pedido → Produção → Entrega → Faturamento → Pagamento
   │          │          │         │          │          │            │
   └── CRM    └── Sales  └── Sales └── MFG    └── LOG    └── FIN     └── FIN
```

## 🎯 Funcionalidades

### ✅ Backend Implementado
- **Autenticação JWT** - Tokens stateless com refresh automático
- **RBAC (Role-Based Access Control)** - 6 roles, permissões granulares (resource.action)
- **Multiempresa (Multi-tenant)** - Suporte para múltiplas empresas via companyId
- **Gestão de Clientes** - CRUD completo com preferências
- **Gestão de Produtos** - Cadastro e controle
- **Gestão de Pedidos** - CRUD + itens + workflow (criar, cancelar)
- **Orçamentos** - Quotes com itens, aprovação e conversão em pedido
- **Comissões** - Cálculo automático de comissões de vendedores
- **Metas de Vendas** - Targets individuais, equipe e empresa
- **Faturamento** - Faturas com itens, status (Draft→Issued→Sent→Paid), cancelamento
- **Pagamentos** - 7 métodos (PIX, Cartão, Boleto, etc.), confirmação, reembolso
- **Estoque** - Controle de inventário, movimentações, níveis mín/máx, motor duplo
- **Armazéns** - Gestão de múltiplos armazéns por empresa
- **Produção (MRP)** - Ordens de produção, BOM (Bill of Materials), workflow completo
- **Entregas** - Logística de expedição e rastreamento
- **Notificações** - Alertas de estoque baixo, pedidos, faturas vencidas
- **Analytics** - Resumo de vendas, receita, top produtos
- **Scheduled Jobs** - Verificação de estoque baixo, faturas vencidas, metas de vendas
- **Global Exception Handler** - Tratamento padronizado de erros na API
- **Database Migrations** - Flyway V1-V22 (22 migrações versionadas)
- **API REST** - Endpoints documentados com Swagger/OpenAPI

### ✅ Frontend Implementado
- **Dashboard** - KPI cards (6 métricas), gráfico de receita/mês, pedidos por status (pizza), alertas de estoque
- **Clientes** - DataGrid profissional + CRUD completo (nome, email, telefone, profissão, preferências)
- **Orçamentos** - DataGrid + CRUD com status (Draft, Sent, Approved, Rejected, Expired)
- **Pedidos** - DataGrid + CRUD + gestão dinâmica de itens (produto, quantidade, preço)
- **Estoque** - DataGrid + CRUD + chips de status (Sem Estoque, Baixo, Normal)
- **Armazéns** - DataGrid + CRUD + desativar armazém
- **Produção** - DataGrid + CRUD + workflow (Iniciar/Finalizar produção)
- **Faturamento** - DataGrid + criação com itens + ações (Emitir, Enviar, Cancelar)
- **Pagamentos** - DataGrid + criação + ações (Confirmar, Cancelar, Reembolsar)
- **Analytics** - Cards de resumo + gráfico de barras (Recharts)
- **Notificações** - Lista por tipo com ícones + marcar como lida
- **Login/Auth** - JWT com refresh token automático
- **RBAC Frontend** - Controle de rotas e ações por role/permission
- **Multi-tenant** - companyId dinâmico em todas as chamadas API

## 🗺️ Roadmap

| Status | Módulo | Descrição |
|--------|--------|-----------|
| ✅ | Security | JWT + RBAC + Multiempresa |
| ✅ | CRM | Gestão de Clientes |
| ✅ | Catalog | Produtos e Estoque |
| ✅ | Orders | Pedidos de Venda + Itens |
| ✅ | Quotes | Orçamentos |
| ✅ | Commissions | Comissões de Vendedores |
| ✅ | Sales Targets | Metas de Vendas |
| ✅ | Invoicing | Faturamento com workflow |
| ✅ | Payments | Pagamentos (7 métodos) |
| ✅ | Inventory | Estoque + Movimentações |
| ✅ | Warehouses | Gestão de Armazéns |
| ✅ | Production | MRP - Ordens de Fabricação |
| ✅ | Delivery | Entregas e Expedição |
| ✅ | Notifications | Alertas e Notificações |
| ✅ | Analytics | Relatórios e KPIs |
| ✅ | Dashboard | Dashboard com gráficos |
| ✅ | Frontend | React SPA completa (11 telas) |
| 🔜 | Docker | Containerização |
| 🔜 | CI/CD | GitHub Actions |
| 🔜 | Testes | Cobertura de testes automatizados |

## 🏗️ Arquitetura

### Stack Tecnológico

#### Backend
- **Runtime**: Java 21 LTS
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6.2.4, JWT (jjwt 0.12.5)
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA, Hibernate 6.4.x
- **Migrations**: Flyway 10.10.0 (22 migrações: V1-V22)
- **API Docs**: Swagger/OpenAPI 2.5.0
- **Build**: Maven 3.9.6

#### Frontend
- **Framework**: React 19 (SPA)
- **Build Tool**: Vite 7.3
- **UI Library**: Material-UI (MUI) 5
- **Tabelas**: MUI X DataGrid (paginação, filtros, ordenação, busca)
- **Gráficos**: Recharts (BarChart, PieChart, LineChart)
- **HTTP Client**: Axios (interceptors JWT automáticos)
- **Roteamento**: react-router-dom 7

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

#### Backend
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
│   ├── scheduler/           # Jobs agendados (estoque, metas, faturas)
│   ├── exception/           # Global exception handler
│   └── config/              # Configurações gerais (DataLoader, etc.)
│
├── model/                   # Entidades de negócio
│   ├── Client.java          # Clientes
│   ├── Product.java         # Produtos
│   ├── Order.java           # Pedidos
│   ├── OrderItem.java       # Itens de pedido
│   └── Project.java         # Projetos customizados
│
├── sales/                   # Módulo de Vendas
│   ├── model/               # Quote, QuoteItem, Commission, SalesTarget
│   ├── dto/                 # Request/Response DTOs
│   └── repository/          # Repositórios JPA
│
├── inventory/               # Módulo de Estoque
│   ├── entity/              # InventoryItem, Warehouse, StockMovement
│   ├── controller/          # InventoryController, WarehouseController
│   ├── repository/          # Repositórios JPA
│   └── service/             # Serviços de inventário e armazéns
│
├── invoicing/               # Módulo de Faturamento
│   ├── entity/              # Invoice, InvoiceItem
│   ├── controller/          # InvoiceController
│   ├── repository/          # Repositórios JPA
│   └── service/             # InvoiceService
│
├── payment/                 # Módulo de Pagamentos
│   ├── entity/              # Payment
│   ├── controller/          # PaymentController
│   └── service/             # PaymentService
│
├── delivery/                # Módulo de Entregas
│   ├── entity/              # Delivery
│   ├── controller/          # DeliveryController
│   └── service/             # DeliveryService
│
├── manufacturing/           # Módulo de Produção (MRP)
│   ├── entity/              # ProductionOrder, BillOfMaterial
│   ├── controller/          # ManufacturingController
│   ├── repository/          # Repositórios JPA
│   └── service/             # ManufacturingService
│
├── notification/            # Módulo de Notificações
│   ├── entity/              # Notification
│   ├── controller/          # NotificationController
│   └── service/             # NotificationService
│
├── analytics/               # Módulo de Analytics
│   ├── dto/                 # SalesSummary, RevenueSummary, TopProduct
│   ├── controller/          # AnalyticsController
│   ├── repository/          # AnalyticsRepository
│   └── service/             # AnalyticsService
│
├── controller/              # REST Controllers (base)
├── service/                 # Serviços de negócio (base)
├── repository/              # Repositórios base
├── dto/                     # DTOs compartilhados
└── config/                  # Configurações (Swagger, CORS, etc.)
```

#### Frontend
```
erp-frontend/src/
│
├── api/
│   └── apiClient.js         # Axios com JWT interceptors + refresh
│
├── auth/
│   ├── AuthContext.jsx       # Provider global (login, roles, companyId)
│   └── permissions.js        # RBAC (hasRouteAccess, canPerform)
│
├── components/
│   ├── Sidebar.jsx           # Menu lateral com navegação
│   └── Topbar.jsx            # Barra superior (usuário, notificações)
│
├── pages/
│   ├── Dashboard/            # KPIs, gráficos, alertas
│   ├── Clients/              # CRUD + DataGrid
│   ├── Quotes/               # CRUD + DataGrid
│   ├── Orders/               # CRUD + DataGrid + itens dinâmicos
│   ├── Inventory/            # CRUD + DataGrid + status chips
│   ├── Warehouses/           # CRUD + DataGrid + desativar
│   ├── Manufacturing/        # CRUD + DataGrid + workflow
│   ├── Invoicing/            # CRUD + DataGrid + ações (emitir/enviar)
│   ├── Payments/             # CRUD + DataGrid + ações
│   ├── Analytics/            # Cards + gráficos
│   ├── Notifications/        # Lista + marcar como lida
│   └── Login/                # Autenticação JWT
│
├── App.jsx                   # Rotas + ProtectedRoute + Layout
└── main.jsx                  # Entry point
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

> O Flyway executa automaticamente todas as migrations (V1-V22) na inicialização.

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
  "fullName": "Administrador",
  "roles": ["ADMIN"],
  "permissions": ["client.create", "client.read", "order.create", "..."],
  "companyId": 1
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
- [**Frontend README**](https://github.com/291294/erp-frontend) - Documentação do frontend React
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
- `order_items` - Itens de pedido

### Módulo de Vendas
- `quotes` - Orçamentos
- `quote_items` - Itens do orçamento
- `commissions` - Comissões de vendedores
- `sales_targets` - Metas de vendas

### Módulo de Inventário
- `inventory_items` - Itens de estoque
- `stock_movements` - Movimentações de estoque
- `warehouses` - Armazéns

### Módulo Financeiro
- `invoices` - Faturas
- `invoice_items` - Itens da fatura
- `payments` - Pagamentos

### Módulo de Produção
- `production_orders` - Ordens de produção
- `bill_of_materials` - Lista de materiais (BOM)
- `bill_of_material_items` - Itens da BOM

### Módulo de Logística
- `deliveries` - Entregas

### Módulo de Notificações
- `notifications` - Notificações do sistema

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

- ✅ **Compilação**: 100% sucesso (backend + frontend)
- ✅ **Vulnerabilidades**: 0 CVEs detectados
- ✅ **Endpoints REST**: 50+ (protegidos por JWT)
- ✅ **Migrations**: 22 (V1-V22)
- ✅ **Tabelas**: 25+
- ✅ **Telas Frontend**: 11 módulos + Dashboard
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
