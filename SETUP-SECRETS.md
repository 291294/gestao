# 🚀 Guia Rápido: Configurar Secrets no GitHub

## Passo 1: Acessar Configurações

1. Abra: **https://github.com/291294/gestao**
2. Clique em **Settings** (Configurações)
3. No menu lateral esquerdo, clique em **Secrets and variables**
4. Clique em **Actions**

## Passo 2: Adicionar Secrets (MÍNIMO para CI funcionar)

### 🔑 Secrets Obrigatórios para CI

O workflow CI **não precisa de secrets** para build/test básicos! Ele vai:
- ✅ Build backend (Java 21 + Maven)
- ✅ Testes JUnit
- ✅ Build frontend (React + Vite)
- ✅ ESLint check

### 🔑 Secrets para CD (Deploy) - Configure quando tiver servidores

Abra o arquivo `secrets-temp.txt` gerado e siga:

#### Para testar CD Staging (quando tiver servidor):

1. Clique em **New repository secret**
2. Adicione cada secret abaixo:

| Nome | Valor | Onde pegar |
|------|-------|------------|
| `STAGING_JWT_SECRET` | Cole o valor de `secrets-temp.txt` | Gerado automaticamente |
| `STAGING_HOST` | Endereço do servidor staging | Ex: `staging.erp-moveis.com` ou `192.168.1.100` |
| `STAGING_USER` | Usuário SSH | Ex: `deployer` |
| `STAGING_SSH_KEY` | Chave privada SSH completa | Gere com: `ssh-keygen -t ed25519 -f ~/.ssh/erp-deploy` |
| `STAGING_DB_NAME` | Nome do banco staging | Ex: `erp_moveis_staging` |
| `STAGING_DB_USER` | Usuário do banco staging | Ex: `erp_user` |
| `STAGING_DB_PASSWORD` | Senha do banco staging | Crie uma senha forte |
| `STAGING_URL` | URL do frontend staging | Ex: `https://staging.erp-moveis.com` |
| `STAGING_API_URL` | URL da API staging | Ex: `https://staging.erp-moveis.com/api` |

#### Para Production (quando tiver servidor):

| Nome | Valor | Onde pegar |
|------|-------|------------|
| `PROD_JWT_SECRET` | Cole o valor de `secrets-temp.txt` | Gerado automaticamente |
| `PROD_HOST` | Endereço do servidor production | Ex: `erp-moveis.com` |
| `PROD_USER` | Usuário SSH | Ex: `prod-deployer` |
| `PROD_SSH_KEY` | Chave privada SSH completa | Gere chave separada para production |
| `PROD_DB_NAME` | Nome do banco production | Ex: `erp_moveis_production` |
| `PROD_DB_USER` | Usuário do banco production | Ex: `erp_prod_user` |
| `PROD_DB_PASSWORD` | Senha FORTE do banco | Use senha muito forte |
| `PROD_URL` | URL do frontend production | Ex: `https://erp-moveis.com` |
| `PROD_API_URL` | URL da API production | Ex: `https://api.erp-moveis.com` |

## Passo 3: Configurar Environments (Aprovações)

1. No GitHub, vá em: **Settings → Environments**
2. Clique em **New environment**

### Criar 4 Environments:

#### 1. `staging`
- **Name**: `staging`
- **Deployment branches**: Selecione "Selected branches" → Adicione `develop`
- **Reviewers**: (deixe vazio - deploy automático)
- Clique **Save**

#### 2. `production`
- **Name**: `production`
- **Deployment branches**: Selecione "Selected branches" → Adicione `main`
- **Reviewers**: ✅ **Adicione seu usuário GitHub** (aprovação obrigatória)
- Clique **Save**

#### 3. `production-approval`
- **Name**: `production-approval`
- **Reviewers**: ✅ **Adicione seu usuário GitHub**
- Clique **Save**

#### 4. `production-rollback-approval`
- **Name**: `production-rollback-approval`
- **Reviewers**: ✅ **Adicione seu usuário GitHub**
- Clique **Save**

## 🧪 Passo 4: Testar Pipeline

### Opção A: Testar apenas CI (sem deploy)

```bash
# O CI já está rodando na branch feature/test-pipeline!
# Veja em: https://github.com/291294/gestao/actions
```

**O que vai acontecer:**
- ✅ Build do backend
- ✅ Testes automatizados
- ✅ Build do frontend
- ✅ Lint check
- ⚠️ Deploy não vai rodar (secrets de servidor não configurados)

### Opção B: Testar Deploy Staging (quando tiver servidor)

```bash
# 1. Fazer merge para develop
git checkout develop
git merge feature/test-pipeline
git push origin develop

# 2. Ver workflow rodando
# https://github.com/291294/gestao/actions
```

### Opção C: Testar Deploy Production (quando tiver servidor)

```bash
# 1. Fazer merge para main
git checkout main
git merge develop
git push origin main

# 2. Aguardar notificação de aprovação
# GitHub vai te notificar

# 3. Aprovar deploy
# Acesso Actions → Workflow rodando → Review deployments
```

## ⚠️ Troubleshooting

### Erro: "Secret not found"
- Verifique se o nome do secret está **exatamente** como na tabela (case-sensitive)
- Exemplo: `STAGING_HOST` (não `staging_host`)

### Erro: "SSH connection failed"
- Verifique se a chave SSH foi adicionada ao servidor: `ssh-copy-id user@servidor`
- Teste manualmente: `ssh -i ~/.ssh/erp-deploy user@servidor`

### Erro: "Database connection failed"
- Verifique se o PostgreSQL está rodando no servidor
- Verifique as credenciais (STAGING_DB_USER, STAGING_DB_PASSWORD)

### Workflow não dispara
- Certifique-se de fazer push para a branch correta:
  - CI: Qualquer branch
  - CD Staging: `develop`
  - CD Production: `main`

## 📚 Referências

- 📖 [Documentação completa do Pipeline](.github/README.md)
- 🔐 [Guia detalhado de Secrets](.github/CICD-SECRETS.md)
- 🐳 [Guia Docker](../DOCKER-QUICKSTART.md)

---

## 🎯 Status Atual

### ✅ Configurado
- [x] Workflows GitHub Actions criados
- [x] Branches (main, develop, feature/test-pipeline)
- [x] JWT Secrets gerados
- [x] CI rodando automaticamente

### ⏳ Pendente
- [ ] Configurar secrets no GitHub (todos os STAGING_* e PROD_*)
- [ ] Configurar environments (staging, production, etc.)
- [ ] Preparar servidor staging
- [ ] Preparar servidor production
- [ ] Testar deploy completo

### 🚀 Próxima Ação

**Para testar CI agora:**
1. Acesse: https://github.com/291294/gestao/actions
2. Veja o workflow rodando (disparado pelo push de `feature/test-pipeline`)

**Para habilitar deploy:**
1. Configure os secrets acima no GitHub
2. Configure os environments
3. Prepare os servidores Linux
