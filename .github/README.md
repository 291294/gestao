# 🚀 CI/CD Pipeline - ERP Móveis

Pipeline completo de **Continuous Integration** e **Continuous Deployment** usando GitHub Actions.

## 📋 Visão Geral

```
┌─────────────────────────────────────────────────────────────┐
│                    CI/CD PIPELINE FLOW                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  1️⃣  CODE PUSH                                               │
│      ↓                                                       │
│  2️⃣  CI - Build & Test (Automático)                          │
│      ├─ Backend: Maven build + Tests                        │
│      ├─ Frontend: npm build + Tests                         │
│      └─ Docker: Build images                                │
│      ↓                                                       │
│  3️⃣  CD - Deploy                                             │
│      ├─ Staging: Auto deploy (branch develop)               │
│      └─ Production: Manual approval (branch main)           │
│      ↓                                                       │
│  4️⃣  Health Checks                                           │
│      ├─ Backend /actuator/health                            │
│      └─ Frontend status                                     │
│      ↓                                                       │
│  5️⃣  Rollback (se necessário)                                │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## 📁 Estrutura de Arquivos

```
.github/
├── workflows/
│   ├── ci.yml                 # Build & Test automático
│   ├── cd-staging.yml         # Deploy para staging
│   ├── cd-production.yml      # Deploy para production
│   └── rollback.yml           # Rollback manual
│
├── CICD-SECRETS.md            # Documentação de secrets
└── README.md                  # Esta documentação
```

---

## 🔄 Workflows

### 1. CI - Continuous Integration (`ci.yml`)

**Trigger**: Push em qualquer branch + Pull Requests

**O que faz**:
- ✅ Build do backend (Java 21 + Maven)
- ✅ Testes automatizados (JUnit)
- ✅ Build do frontend (React + Vite)
- ✅ Lint check (ESLint)
- ✅ Build das imagens Docker
- ✅ Upload de artefatos

**Tempo médio**: ~5-8 minutos

**Status necessário**: ✅ PASS para merge

```yaml
# Exemplo de trigger
push:
  branches: ['**']
pull_request:
  branches: [main, develop]
```

---

### 2. CD Staging (`cd-staging.yml`)

**Trigger**: Push para branch `develop`

**O que faz**:
1. Build & Push de imagens Docker para GitHub Container Registry
2. Deploy automático para servidor de staging
3. Health checks pós-deploy
4. Rollback automático se falhar

**Aprovação**: ❌ Não necessária (automático)

**Tempo médio**: ~10-15 minutos

**URL**: Configurada em `STAGING_URL`

```yaml
# Exemplo de deploy
environment:
  name: staging
  url: ${{ secrets.STAGING_URL }}
```

---

### 3. CD Production (`cd-production.yml`)

**Trigger**: 
- Push para branch `main`
- Tags `v*.*.*`
- Manual (workflow_dispatch)

**O que faz**:
1. Validações pré-deploy
2. Build & Push de imagens Docker
3. **🔐 Aguarda aprovação manual**
4. Backup da versão atual
5. Deploy para produção
6. Health checks rigorosos
7. Rollback automático se falhar

**Aprovação**: ✅ **OBRIGATÓRIA** (manual)

**Tempo médio**: ~15-20 minutos (+ tempo de aprovação)

**Estratégia**: Blue-Green deployment

```yaml
# Aprovação obrigatória
environment:
  name: production-approval
```

---

### 4. Rollback (`rollback.yml`)

**Trigger**: Manual (workflow_dispatch)

**O que faz**:
- Rollback para versão anterior ou específica
- Suporta staging e production
- Aprovação obrigatória para production
- Health checks pós-rollback
- Registro de eventos

**Quando usar**:
- ❌ Bug crítico em produção
- ❌ Performance degradada
- ❌ Falha de segurança

```yaml
# Exemplo de uso
inputs:
  environment: staging | production
  version: v1.2.3 (opcional)
  reason: "Descrição do motivo"
```

---

## 🌍 Ambientes

### Staging
- **Branch**: `develop`
- **Deploy**: Automático
- **Aprovação**: Não
- **URL**: `STAGING_URL`
- **Uso**: Testes internos, QA

### Production
- **Branch**: `main`
- **Deploy**: Manual
- **Aprovação**: ✅ Sim (obrigatória)
- **URL**: `PROD_URL`
- **Uso**: Clientes reais

---

## ⚙️ Configuração Inicial

### 1. Configurar Secrets

Siga o guia completo: [CICD-SECRETS.md](CICD-SECRETS.md)

**Secrets obrigatórios**:
- SSH: `*_HOST`, `*_USER`, `*_SSH_KEY`
- Database: `*_DB_NAME`, `*_DB_USER`, `*_DB_PASSWORD`
- JWT: `*_JWT_SECRET`
- URLs: `*_URL`, `*_API_URL`

### 2. Configurar Environments

No GitHub:
```
Settings → Environments → New environment
```

Crie:
- `staging`
- `production` (com reviewers)
- `production-approval` (com reviewers)
- `production-rollback-approval` (com reviewers)

### 3. Preparar Servidores

**Staging e Production precisam**:
- Docker e Docker Compose instalados
- Acesso SSH configurado
- Firewall com portas abertas (80, 443, 8080, 5432)
- DNS configurado

---

## 🚀 Como Usar

### Deploy para Staging

```bash
# 1. Desenvolva em sua branch
git checkout -b feature/nova-funcionalidade

# 2. Commit e push
git add .
git commit -m "Nova funcionalidade"
git push origin feature/nova-funcionalidade

# 3. Abra Pull Request para develop
# CI vai rodar automaticamente

# 4. Após merge em develop
# CD Staging roda automaticamente
```

### Deploy para Production

```bash
# 1. Merge develop → main (após teste em staging)
git checkout main
git merge develop
git push origin main

# 2. CD Production inicia automaticamente

# 3. Acesse GitHub Actions

# 4. Aprove o deploy manualmente
# (Botão "Review deployments")

# 5. Aguarde deploy completar
```

### Rollback

```bash
# No GitHub:
# Actions → Rollback Deployment → Run workflow

# Preencha:
# - Environment: staging | production
# - Version: v1.2.3 (ou deixe vazio para versão anterior)
# - Reason: "Motivo do rollback"

# Para production: Aprovar rollback manualmente
```

---

## 📊 Monitoramento

### Verificar Status do Pipeline

1. **GitHub Actions Tab**: Ver runs em andamento
2. **Branches**: Ver status do último commit
3. **Environments**: Ver deploys ativos

### Logs

```bash
# Durante deploy, acesse:
Actions → Workflow específico → Job → Steps

# No servidor (SSH):
cd ~/erp-staging  # ou ~/erp-production
docker-compose logs -f
```

### Health Checks

```bash
# Staging
curl https://staging.erp-moveis.com/api/actuator/health

# Production
curl https://erp-moveis.com/api/actuator/health
```

---

## 🔧 Troubleshooting

### CI Falhou

**Problemas comuns**:
- ❌ Testes falharam → Corrigir código
- ❌ Build falhou → Verificar dependências
- ❌ Docker build falhou → Verificar Dockerfile

**Solução**: Corrija e faça novo commit

### CD Staging Falhou

**Problemas comuns**:
- ❌ SSH connection failed → Verificar secrets
- ❌ Docker pull failed → Verificar registry
- ❌ Health check failed → Verificar logs no servidor

**Solução**: 
```bash
# SSH no servidor
ssh user@staging-server
cd ~/erp-staging
docker-compose logs
```

### CD Production Falhou

**Ação imediata**:
1. Verificar health checks
2. Se necessário, executar rollback
3. Investigar causa raiz
4. Corrigir em staging primeiro

### Rollback Falhou

**❌ CRÍTICO**: Intervenção manual necessária

```bash
# SSH no servidor
ssh user@production-server
cd ~/erp-production

# Verificar backups disponíveis
ls -la ~/erp-backups/

# Restaurar manualmente
cp ~/erp-backups/docker-compose.yml.TIMESTAMP docker-compose.yml
docker-compose down
docker-compose up -d
```

---

## 📈 Melhorias Futuras

### Fase 2 (Curto Prazo)
- [ ] Testes de integração E2E
- [ ] Smoke tests pós-deploy
- [ ] Métricas de performance
- [ ] Notificações Slack/Discord
- [ ] Ambientes de review por PR

### Fase 3 (Médio Prazo)
- [ ] Deploy canary (5% → 100%)
- [ ] Feature flags
- [ ] A/B testing
- [ ] Análise de código (SonarQube)
- [ ] Security scanning

### Fase 4 (Longo Prazo)
- [ ] Multi-region deployment
- [ ] Auto-scaling
- [ ] Disaster recovery
- [ ] Chaos engineering

---

## 📚 Recursos Adicionais

- [Documentação GitHub Actions](https://docs.github.com/en/actions)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [12 Factor App](https://12factor.net/)

---

## 🤝 Contribuindo

### Modificar Workflows

1. Teste localmente com [act](https://github.com/nektos/act)
2. Crie branch `ci/melhoria-workflow`
3. Teste em staging primeiro
4. Pull request com descrição detalhada

### Adicionar Novo Environment

1. Criar secrets necessários
2. Duplicar workflow existente
3. Ajustar configurações
4. Testar deploy

---

## 📞 Suporte

**Para questões sobre CI/CD**:
- DevOps Team Lead: devops@erp-moveis.com
- Documentação: Esta pasta
- Issues: GitHub Issues

---

**Última atualização**: 10/03/2026  
**Versão do Pipeline**: 1.0.0
