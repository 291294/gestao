# Pipeline CI/CD - Teste

Este arquivo foi criado para testar o workflow de CI/CD do GitHub Actions.

## Status do Teste

- ✅ Branch feature/test-pipeline criada
- ✅ Push realizado
- ⏳ Aguardando execução do workflow CI

## Workflow CI Esperado

O workflow `.github/workflows/ci.yml` deve:
1. Build do backend (Java 21 + Maven)
2. Executar testes (JUnit)
3. Build do frontend (React + Vite)
4. Lint check (ESLint)
5. Build das imagens Docker
6. Quality gate

## Próximos Passos

Depois que o CI passar:
1. Merge para `develop` → Dispara CD Staging (se secrets configurados)
2. Merge `develop` → `main` → Dispara CD Production (com aprovação manual)

---
**Data**: 10/03/2026
**Autor**: Teste automatizado
