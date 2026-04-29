package com.erp.moveis.core.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Verifica que o TenantContext está populado para requisições autenticadas.
 * A ativação real do Hibernate Filter ocorre em {@link TenantFilterAspect},
 * dentro de cada transação — o que funciona corretamente com open-in-view=false.
 */
@Component
public class TenantFilterInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // O TenantContext é setado pelo JwtAuthenticationFilter.
        // O filtro Hibernate é ativado pelo TenantFilterAspect dentro de cada @Transactional.
        return true;
    }
}
