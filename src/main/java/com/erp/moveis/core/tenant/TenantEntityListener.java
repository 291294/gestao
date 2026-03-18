package com.erp.moveis.core.tenant;

import jakarta.persistence.PrePersist;

/**
 * JPA Entity Listener that automatically sets the companyId
 * from the current TenantContext before persisting any entity
 * that implements TenantAware.
 */
public class TenantEntityListener {

    @PrePersist
    public void setTenantOnCreate(Object entity) {
        if (entity instanceof TenantAware tenantAware) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null && tenantAware.getCompanyId() == null) {
                tenantAware.setCompanyId(tenantId);
            }
        }
    }
}
