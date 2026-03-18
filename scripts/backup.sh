#!/bin/bash
# ============================================================
# Backup Script - ERP Móveis PostgreSQL
# ============================================================
# Uso: ./backup.sh [daily|weekly|manual]
# Crontab:
#   0 2 * * * /opt/erp-moveis/scripts/backup.sh daily
#   0 3 * * 0 /opt/erp-moveis/scripts/backup.sh weekly
# ============================================================

set -euo pipefail

BACKUP_TYPE="${1:-manual}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="${BACKUP_DIR:-/opt/erp-moveis/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-erp_moveis}"
DB_USER="${DB_USER:-postgres}"
LOG_FILE="${BACKUP_DIR}/backup.log"

mkdir -p "${BACKUP_DIR}/${BACKUP_TYPE}"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "${LOG_FILE}"
}

log "=== Iniciando backup ${BACKUP_TYPE} ==="

BACKUP_FILE="${BACKUP_DIR}/${BACKUP_TYPE}/${DB_NAME}_${BACKUP_TYPE}_${TIMESTAMP}.sql.gz"

pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" \
    --format=custom \
    --compress=9 \
    --verbose \
    --no-owner \
    --no-privileges \
    2>> "${LOG_FILE}" | gzip > "${BACKUP_FILE}"

if [ $? -eq 0 ]; then
    FILESIZE=$(du -sh "${BACKUP_FILE}" | cut -f1)
    log "Backup concluído: ${BACKUP_FILE} (${FILESIZE})"
else
    log "ERRO: Falha no backup!"
    exit 1
fi

# Limpeza de backups antigos
log "Removendo backups com mais de ${RETENTION_DAYS} dias..."
find "${BACKUP_DIR}" -name "*.sql.gz" -type f -mtime +${RETENTION_DAYS} -delete 2>/dev/null || true
REMAINING=$(find "${BACKUP_DIR}" -name "*.sql.gz" -type f | wc -l)
log "Backups restantes: ${REMAINING}"

log "=== Backup ${BACKUP_TYPE} finalizado ==="
