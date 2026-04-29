package com.erp.moveis.core.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Session;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Ativa o Hibernate tenant filter DENTRO de cada transação.
 *
 * Graças ao @EnableTransactionManagement(order = MAX_VALUE - 100) em Application.java,
 * o interceptor de transação tem prioridade maior (order menor) que este aspecto.
 * Assim a sequência é:
 *   1. TransactionInterceptor inicia a transação (abre a Session)
 *   2. TenantFilterAspect ativa o filtro na Session ativa
 *   3. Método do service executa
 *   4. TransactionInterceptor commit/rollback
 *
 * Isso garante funcionamento correto mesmo com spring.jpa.open-in-view=false.
 */
@Aspect
@Component
@Order(Integer.MAX_VALUE)
@Slf4j
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object applyTenantFilter(ProceedingJoinPoint joinPoint) throws Throwable {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            try {
                Session session = entityManager.unwrap(Session.class);
                if (session.getEnabledFilter("tenantFilter") == null) {
                    session.enableFilter("tenantFilter")
                           .setParameter("companyId", tenantId);
                }
            } catch (Exception e) {
                // Se não houver sessão ativa (ex.: scheduler sem transação), ignora silenciosamente
                log.trace("[TENANT_FILTER] Não foi possível ativar filter: {}", e.getMessage());
            }
        }
        return joinPoint.proceed();
    }
}
