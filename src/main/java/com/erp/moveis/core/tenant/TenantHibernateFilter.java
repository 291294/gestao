package com.erp.moveis.core.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Componente responsável por ativar o Hibernate Filter de tenant.
 * Quando enable() é chamado, todas as queries JPA/Hibernate
 * são automaticamente filtradas por company_id.
 */
@Component
public class TenantHibernateFilter {

    @PersistenceContext
    private EntityManager entityManager;

    public void enable() {
        Long tenantId = TenantContext.requireTenantId();
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter")
               .setParameter("companyId", tenantId);
    }
}
