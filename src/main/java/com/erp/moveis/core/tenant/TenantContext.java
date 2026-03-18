package com.erp.moveis.core.tenant;

import org.slf4j.MDC;

/**
 * ThreadLocal holder for the current tenant (company) ID.
 * Set by JwtAuthenticationFilter on each authenticated request
 * and cleared automatically after the request completes.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final String MDC_TENANT_KEY = "tenantId";

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
        if (tenantId != null) {
            MDC.put(MDC_TENANT_KEY, tenantId.toString());
        }
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static Long requireTenantId() {
        Long tenantId = CURRENT_TENANT.get();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant ID not set in current context");
        }
        return tenantId;
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        MDC.remove(MDC_TENANT_KEY);
    }
}
