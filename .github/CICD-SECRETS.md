# 🚀 CI/CD Pipeline - Configuração de Secrets

Este documento descreve todos os **GitHub Secrets** necessários para o pipeline CI/CD funcionar corretamente.

## 📋 Índice
- [Secrets Compartilhados](#secrets-compartilhados)
- [Secrets - Staging](#secrets---staging)
- [Secrets - Production](#secrets---production)
- [Como Configurar](#como-configurar)
- [Environments](#environments)

---

## 🔐 Secrets Compartilhados

Estes secrets são usados em múltiplos ambientes:

| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `GITHUB_TOKEN` | Token automático do GitHub | *Gerado automaticamente* |

---

## 🧪 Secrets - Staging

Configure estes secrets para o ambiente de **staging**:

### SSH/Deploy
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `STAGING_HOST` | IP ou hostname do servidor | `staging.erp-moveis.com` |
| `STAGING_USER` | Usuário SSH | `deployer` |
| `STAGING_SSH_KEY` | Chave privada SSH | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `STAGING_PORT` | Porta SSH (opcional) | `22` |

### URLs
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `STAGING_URL` | URL do frontend | `https://staging.erp-moveis.com` |
| `STAGING_API_URL` | URL da API | `https://staging.erp-moveis.com/api` |

### Database
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `STAGING_DB_NAME` | Nome do banco de dados | `erp_moveis_staging` |
| `STAGING_DB_USER` | Usuário do banco | `erp_user` |
| `STAGING_DB_PASSWORD` | Senha do banco | `SuperSecurePassword123!` |

### JWT/Security
| Secret | Descrição | Como Gerar |
|--------|-----------|------------|
| `STAGING_JWT_SECRET` | Secret para JWT | `openssl rand -base64 64` |

---

## 🚀 Secrets - Production

Configure estes secrets para o ambiente de **production**:

### SSH/Deploy
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `PROD_HOST` | IP ou hostname do servidor | `erp-moveis.com` |
| `PROD_USER` | Usuário SSH | `prod-deployer` |
| `PROD_SSH_KEY` | Chave privada SSH | `-----BEGIN OPENSSH PRIVATE KEY-----...` |
| `PROD_PORT` | Porta SSH (opcional) | `22` |

### URLs
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `PROD_URL` | URL do frontend | `https://erp-moveis.com` |
| `PROD_API_URL` | URL da API | `https://api.erp-moveis.com` |

### Database
| Secret | Descrição | Exemplo |
|--------|-----------|---------|
| `PROD_DB_NAME` | Nome do banco de dados | `erp_moveis_production` |
| `PROD_DB_USER` | Usuário do banco | `erp_prod_user` |
| `PROD_DB_PASSWORD` | Senha forte do banco | `VeryStr0ng!P@ssw0rd#2024` |

### JWT/Security
| Secret | Descrição | Como Gerar |
|--------|-----------|------------|
| `PROD_JWT_SECRET` | Secret para JWT (FORTE) | `openssl rand -base64 96` |
| `PROD_JWT_EXPIRATION` | Tempo expiração token (ms) | `86400000` (24h) |

---

## ⚙️ Como Configurar

### 1. Acessar GitHub Secrets

```
Repository → Settings → Secrets and variables → Actions → New repository secret
```

### 2. Adicionar Cada Secret

Para cada secret da tabela acima:

1. Clique em **"New repository secret"**
2. Coloque o **Nome** exatamente como na tabela (ex: `STAGING_HOST`)
3. Cole o **Valor** correspondente
4. Clique em **"Add secret"**

### 3. Gerar Chave SSH (se necessário)

```bash
# No seu computador local
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/erp-deploy

# Adicionar chave pública ao servidor
ssh-copy-id -i ~/.ssh/erp-deploy.pub user@servidor

# Copiar chave privada para usar como secret
cat ~/.ssh/erp-deploy
# Cole todo o conteúdo no secret STAGING_SSH_KEY ou PROD_SSH_KEY
```

### 4. Gerar JWT Secret Seguro

```bash
# Para Staging
openssl rand -base64 64

# Para Production (ainda mais seguro)
openssl rand -base64 96
```

---

## 🌍 Environments

Configure os **Environments** no GitHub para aprovações manuais:

### 1. Criar Environments

```
Repository → Settings → Environments → New environment
```

Crie os seguintes environments:

#### `staging`
- **Deployment branches**: `develop`
- **Reviewers**: Não necessário (deploy automático)

#### `production`
- **Deployment branches**: `main`
- **Reviewers**: ✅ Adicione revisores (você + outros)
- **Wait timer**: 0 minutos

#### `production-approval`
- **Reviewers**: ✅ Adicione revisores
- **Wait timer**: 0 minutos

#### `production-rollback-approval`
- **Reviewers**: ✅ Adicione revisores (rollback crítico)
- **Wait timer**: 0 minutos

---

## 🔒 Boas Práticas de Segurança

### ✅ DO (Faça)
- ✅ Use senhas fortes e únicas para produção
- ✅ Rotacione secrets periodicamente (a cada 90 dias)
- ✅ Use diferentes secrets para staging e production
- ✅ Mantenha chaves SSH protegidas
- ✅ Use HTTPS/TLS em produção
- ✅ Limite acesso aos secrets (apenas admins)

### ❌ DON'T (Não Faça)
- ❌ Nunca commite secrets no código
- ❌ Não use senhas fracas ou padrão
- ❌ Não compartilhe secrets por email/chat
- ❌ Não reutilize secrets entre ambientes
- ❌ Não dê acesso desnecessário aos secrets

---

## 🧪 Testar Configuração

Após configurar todos os secrets, teste o pipeline:

### 1. Testar CI (Build & Test)
```bash
git push origin feature/teste
# Verifica se CI passa
```

### 2. Testar Deploy Staging
```bash
git push origin develop
# Verifica se deploy staging funciona
```

### 3. Testar Deploy Production
```bash
git push origin main
# Verifica aprovação manual e deploy
```

---

## 📊 Checklist de Configuração

Use este checklist para garantir que tudo está configurado:

### Secrets - Staging
- [ ] `STAGING_HOST`
- [ ] `STAGING_USER`
- [ ] `STAGING_SSH_KEY`
- [ ] `STAGING_URL`
- [ ] `STAGING_API_URL`
- [ ] `STAGING_DB_NAME`
- [ ] `STAGING_DB_USER`
- [ ] `STAGING_DB_PASSWORD`
- [ ] `STAGING_JWT_SECRET`

### Secrets - Production
- [ ] `PROD_HOST`
- [ ] `PROD_USER`
- [ ] `PROD_SSH_KEY`
- [ ] `PROD_URL`
- [ ] `PROD_API_URL`
- [ ] `PROD_DB_NAME`
- [ ] `PROD_DB_USER`
- [ ] `PROD_DB_PASSWORD`
- [ ] `PROD_JWT_SECRET`
- [ ] `PROD_JWT_EXPIRATION`

### Environments
- [ ] Environment `staging` criado
- [ ] Environment `production` criado (com reviewers)
- [ ] Environment `production-approval` criado (com reviewers)
- [ ] Environment `production-rollback-approval` criado (com reviewers)

---

## 🆘 Troubleshooting

### Erro: "Secret not found"
**Solução**: Verifique se o nome do secret está exatamente igual (case-sensitive)

### Erro: "SSH connection failed"
**Solução**: 
1. Verifique se a chave SSH está correta
2. Teste conexão: `ssh -i chave usuario@servidor`
3. Verifique firewall do servidor

### Erro: "Health check failed"
**Solução**:
1. Verifique se as URLs estão corretas
2. Verifique se os serviços subiram corretamente
3. Veja logs: `docker-compose logs`

---

## 📞 Suporte

Para problemas com CI/CD:
1. Verifique logs do GitHub Actions
2. Verifique secrets configurados
3. Teste conexão SSH manualmente
4. Contate o administrador do repositório

---

**Última atualização**: 10/03/2026
