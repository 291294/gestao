#!/bin/bash
# ============================================================================
# ERP Moveis - Server Setup Script
# ============================================================================
# Uso: curl -fsSL <url>/setup-server.sh | bash -s -- [staging|production]
# Ou:  ./setup-server.sh staging
# ============================================================================

set -euo pipefail

ENVIRONMENT=${1:-staging}
DEPLOY_USER="deployer"
APP_DIR="/home/${DEPLOY_USER}/erp-${ENVIRONMENT}"
BACKUP_DIR="/home/${DEPLOY_USER}/backups"

echo "============================================"
echo "  ERP Moveis - Server Setup (${ENVIRONMENT})"
echo "============================================"
echo ""

# Verificar se esta rodando como root
if [ "$(id -u)" -ne 0 ]; then
    echo "ERRO: Execute como root (sudo)"
    exit 1
fi

# -------------------------------------------
# 1. Atualizar sistema
# -------------------------------------------
echo "[1/8] Atualizando sistema..."
apt-get update -qq
apt-get upgrade -y -qq
echo "OK - Sistema atualizado"

# -------------------------------------------
# 2. Instalar Docker
# -------------------------------------------
echo "[2/8] Instalando Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
    sh /tmp/get-docker.sh
    rm /tmp/get-docker.sh
    echo "OK - Docker instalado"
else
    echo "OK - Docker ja instalado ($(docker --version))"
fi

# -------------------------------------------
# 3. Instalar Docker Compose Plugin
# -------------------------------------------
echo "[3/8] Instalando Docker Compose..."
if ! docker compose version &> /dev/null; then
    apt-get install -y -qq docker-compose-plugin
    echo "OK - Docker Compose instalado"
else
    echo "OK - Docker Compose ja instalado ($(docker compose version))"
fi

# -------------------------------------------
# 4. Criar usuario deployer
# -------------------------------------------
echo "[4/8] Configurando usuario deployer..."
if ! id "${DEPLOY_USER}" &>/dev/null; then
    useradd -m -s /bin/bash "${DEPLOY_USER}"
    usermod -aG docker "${DEPLOY_USER}"
    echo "OK - Usuario ${DEPLOY_USER} criado"
else
    usermod -aG docker "${DEPLOY_USER}"
    echo "OK - Usuario ${DEPLOY_USER} ja existe"
fi

# -------------------------------------------
# 5. Configurar SSH
# -------------------------------------------
echo "[5/8] Configurando SSH..."

# Instalar OpenSSH Server se necessario
if ! command -v sshd &> /dev/null; then
    apt-get install -y -qq openssh-server
fi

# Criar diretorio .ssh para deployer
DEPLOYER_HOME="/home/${DEPLOY_USER}"
mkdir -p "${DEPLOYER_HOME}/.ssh"
chmod 700 "${DEPLOYER_HOME}/.ssh"
touch "${DEPLOYER_HOME}/.ssh/authorized_keys"
chmod 600 "${DEPLOYER_HOME}/.ssh/authorized_keys"
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${DEPLOYER_HOME}/.ssh"

# Configurar sshd para seguranca
SSHD_CONFIG="/etc/ssh/sshd_config"
if ! grep -q "# ERP-MOVEIS-CONFIG" "${SSHD_CONFIG}"; then
    cat >> "${SSHD_CONFIG}" << 'SSHEOF'

# ERP-MOVEIS-CONFIG
PubkeyAuthentication yes
PasswordAuthentication no
PermitRootLogin no
MaxAuthTries 3
SSHEOF
fi

# Iniciar SSH
service ssh start 2>/dev/null || systemctl start sshd 2>/dev/null || true
echo "OK - SSH configurado"

# -------------------------------------------
# 6. Criar diretorios da aplicacao
# -------------------------------------------
echo "[6/8] Criando diretorios..."
mkdir -p "${APP_DIR}"
mkdir -p "${BACKUP_DIR}"
mkdir -p "${DEPLOYER_HOME}/logs"
chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${APP_DIR}" "${BACKUP_DIR}" "${DEPLOYER_HOME}/logs"
echo "OK - Diretorios criados"

# -------------------------------------------
# 7. Configurar firewall
# -------------------------------------------
echo "[7/8] Configurando firewall..."
if command -v ufw &> /dev/null; then
    ufw allow 22/tcp   # SSH
    ufw allow 80/tcp   # HTTP
    ufw allow 443/tcp  # HTTPS
    ufw allow 3000/tcp # Frontend
    ufw allow 8080/tcp # Backend
    # NAO expor PostgreSQL externamente (5432)
    echo "OK - Firewall configurado"
else
    echo "SKIP - UFW nao encontrado"
fi

# -------------------------------------------
# 8. Criar docker-compose base
# -------------------------------------------
echo "[8/8] Criando docker-compose base..."
cat > "${APP_DIR}/docker-compose.yml" << DCEOF
services:
  postgres:
    image: postgres:17-alpine
    environment:
      POSTGRES_DB: erp_moveis_${ENVIRONMENT}
      POSTGRES_USER: \${DB_USER:-erp_user}
      POSTGRES_PASSWORD: \${DB_PASSWORD:-changeme}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U \${DB_USER:-erp_user} -d erp_moveis_${ENVIRONMENT}"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    image: \${BACKEND_IMAGE:-erp-backend:latest}
    environment:
      SPRING_PROFILES_ACTIVE: ${ENVIRONMENT}
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/erp_moveis_${ENVIRONMENT}
      SPRING_DATASOURCE_USERNAME: \${DB_USER:-erp_user}
      SPRING_DATASOURCE_PASSWORD: \${DB_PASSWORD:-changeme}
      JWT_SECRET: \${JWT_SECRET:-changeme}
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 5
      start_period: 90s

  frontend:
    image: \${FRONTEND_IMAGE:-erp-frontend:latest}
    ports:
      - "3000:80"
    depends_on:
      - backend
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--spider", "-q", "http://localhost/"]
      interval: 15s
      timeout: 5s
      retries: 3

volumes:
  postgres-data:
DCEOF

# Criar .env template
cat > "${APP_DIR}/.env" << ENVEOF
# ERP Moveis - ${ENVIRONMENT} Environment
# Gerado automaticamente em $(date)

# Database
DB_USER=erp_user
DB_PASSWORD=ALTERE_ESTA_SENHA

# JWT
JWT_SECRET=ALTERE_ESTE_SECRET

# Docker Images (atualizadas pelo CD pipeline)
BACKEND_IMAGE=erp-backend:latest
FRONTEND_IMAGE=erp-frontend:latest
ENVEOF

chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${APP_DIR}"
echo "OK - Docker Compose criado"

# -------------------------------------------
# Resumo
# -------------------------------------------
echo ""
echo "============================================"
echo "  SETUP COMPLETO!"
echo "============================================"
echo ""
echo "  Ambiente: ${ENVIRONMENT}"
echo "  Usuario:  ${DEPLOY_USER}"
echo "  App Dir:  ${APP_DIR}"
echo "  Backups:  ${BACKUP_DIR}"
echo ""
echo "  Proximo passo:"
echo "  1. Adicionar chave SSH publica em:"
echo "     ${DEPLOYER_HOME}/.ssh/authorized_keys"
echo ""
echo "  2. Editar ${APP_DIR}/.env com senhas reais"
echo ""
echo "  3. Testar conexao SSH:"
echo "     ssh ${DEPLOY_USER}@<SERVER_IP>"
echo ""
echo "============================================"
