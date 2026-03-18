package com.erp.moveis.core.tenant;

/**
 * Interface for entities that belong to a specific tenant (company).
 * Entities implementing this will automatically have companyId set
 * via TenantEntityListener on persist.
 */
public interface TenantAware {
    Long getCompanyId();
    void setCompanyId(Long companyId);
}
