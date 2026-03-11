# Script para gerar secrets necessarios para CI/CD

Write-Host "Gerando Secrets para CI/CD Pipeline" -ForegroundColor Green
Write-Host ""

# Gerar JWT Secrets
Write-Host "JWT SECRETS:" -ForegroundColor Cyan
Write-Host ""

# Staging JWT (64 bytes)
$bytes64 = New-Object byte[] 64
$rng = [Security.Cryptography.RNGCryptoServiceProvider]::Create()
$rng.GetBytes($bytes64)
$stagingJWT = [Convert]::ToBase64String($bytes64)

# Production JWT (96 bytes - mais seguro)
$bytes96 = New-Object byte[] 96
$rng.GetBytes($bytes96)
$prodJWT = [Convert]::ToBase64String($bytes96)

Write-Host "STAGING_JWT_SECRET:" -ForegroundColor Yellow
Write-Host $stagingJWT
Write-Host ""

Write-Host "PROD_JWT_SECRET:" -ForegroundColor Yellow
Write-Host $prodJWT
Write-Host ""

# Salvar em arquivo para referencia (NAO COMMITAR!)
$secretsFile = "secrets-temp.txt"
$content = @"
# SECRETS GERADOS - NAO COMMITAR ESTE ARQUIVO!
# Data: $(Get-Date -Format "dd/MM/yyyy HH:mm")

## STAGING
STAGING_JWT_SECRET=$stagingJWT

## PRODUCTION  
PROD_JWT_SECRET=$prodJWT

## CONFIGURACOES DE EXEMPLO (ALTERE CONFORME SEU AMBIENTE)

# Staging Database (quando tiver servidor)
STAGING_DB_NAME=erp_moveis_staging
STAGING_DB_USER=erp_user
STAGING_DB_PASSWORD=GERE_UMA_SENHA_FORTE_AQUI

# Production Database (quando tiver servidor)
PROD_DB_NAME=erp_moveis_production
PROD_DB_USER=erp_prod_user
PROD_DB_PASSWORD=GERE_UMA_SENHA_MUITO_FORTE_AQUI

# Staging Server (quando tiver servidor)
STAGING_HOST=seu-servidor-staging.com
STAGING_USER=deployer
STAGING_PORT=22
STAGING_URL=https://staging.erp-moveis.com
STAGING_API_URL=https://staging.erp-moveis.com/api

# Production Server (quando tiver servidor)
PROD_HOST=seu-servidor-production.com
PROD_USER=prod-deployer
PROD_PORT=22
PROD_URL=https://erp-moveis.com
PROD_API_URL=https://api.erp-moveis.com

# JWT Expiration
PROD_JWT_EXPIRATION=86400000
STAGING_JWT_EXPIRATION=86400000

"@

$content | Out-File -FilePath $secretsFile -Encoding UTF8

Write-Host "Secrets salvos em: $secretsFile" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANTE: Adicione 'secrets-temp.txt' ao .gitignore!" -ForegroundColor Red
Write-Host ""

# Adicionar ao .gitignore se nao existir
$gitignorePath = ".gitignore"
if (Test-Path $gitignorePath) {
    $gitignoreContent = Get-Content $gitignorePath -Raw
    if ($gitignoreContent -notmatch "secrets-temp") {
        Add-Content -Path $gitignorePath -Value "`n# Secrets temporarios`nsecrets-temp.txt"
        Write-Host "secrets-temp.txt adicionado ao .gitignore" -ForegroundColor Green
    }
} else {
    "secrets-temp.txt" | Out-File -FilePath $gitignorePath
    Write-Host ".gitignore criado com secrets-temp.txt" -ForegroundColor Green
}

Write-Host ""
Write-Host "PROXIMOS PASSOS:" -ForegroundColor Cyan
Write-Host "1. Abra o arquivo secrets-temp.txt" -ForegroundColor White
Write-Host "2. Copie os valores gerados" -ForegroundColor White
Write-Host "3. Configure no GitHub: Settings -> Secrets and variables -> Actions" -ForegroundColor White
Write-Host "4. Para testar apenas CI (sem deploy), configure apenas os JWT secrets" -ForegroundColor White
Write-Host ""
