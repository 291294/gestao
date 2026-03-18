package com.erp.moveis.core.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que ativa o Hibernate Filter de tenant
 * automaticamente em cada request autenticado.
 * Delega para TenantHibernateFilter.enable().
 */
@Component
public class TenantFilterInterceptor implements HandlerInterceptor {

    private final TenantHibernateFilter tenantHibernateFilter;

    public TenantFilterInterceptor(TenantHibernateFilter tenantHibernateFilter) {
        this.tenantHibernateFilter = tenantHibernateFilter;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId != null) {
            tenantHibernateFilter.enable();
        }
        return true;
    }
}
