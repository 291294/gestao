package com.erp.moveis.core.security.jwt;

import com.erp.moveis.core.config.MetricsConfig;
import com.erp.moveis.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro JWT com suporte a modo dual de autenticação:
 *
 * <ol>
 *   <li><b>HttpOnly Cookie</b> (preferencial) — {@code erp_access_token}: seguro contra XSS,
 *       gerenciado pelo browser, invisível ao JavaScript.</li>
 *   <li><b>Authorization: Bearer</b> (compatibilidade) — header HTTP clássico, mantido durante
 *       a migração para não quebrar sessões existentes. Remover após conclusão da migração.</li>
 * </ol>
 *
 * <p>Estratégia de rollout: o cookie tem prioridade. Se presente e válido, o header é ignorado.
 * Sessões antigas com token em localStorage continuam funcionando via Bearer até expirarem.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Nome do cookie de acesso (configurável via application.properties)
    @Value("${cookie.access.name:erp_access_token}")
    private String accessCookieName;

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final MetricsConfig.ErpMetrics metrics;

    private static final Set<String> TENANT_EXEMPT_PREFIXES = Set.of(
            "/auth/", "/swagger-ui", "/v3/api-docs", "/actuator", "/h2-console"
    );

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            MetricsConfig.ErpMetrics metrics) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.metrics = metrics;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extrai JWT de qualquer fonte suportada (cookie > header)
        final String jwt = extractJwt(request);

        if (jwt == null) {
            // Sem token → continua a cadeia; Spring Security trata 401 para rotas protegidas
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Propaga companyId para o TenantContext (multi-tenancy)
                    Long companyId = jwtService.extractCompanyId(jwt);
                    if (companyId != null) {
                        TenantContext.setTenantId(companyId);
                    } else if (!isTenantExempt(request)) {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Tenant not associated with user\"}");
                        return;
                    }
                }
            }
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // JWT inválido, expirado ou malformado — incrementa métrica e continua sem autenticar.
            // Não lançar exceção: o Spring Security rejeitará a requisição protegida com 401.
            metrics.jwtInvalid.increment();
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * Extrai o JWT de forma priorizada e segura.
     *
     * <ol>
     *   <li>Cookie HttpOnly {@code erp_access_token} (modo seguro — imune a XSS)</li>
     *   <li>Header {@code Authorization: Bearer ...} (modo legado — migração gradual)</li>
     * </ol>
     *
     * @return token JWT ou {@code null} se ausente em todas as fontes
     */
    private String extractJwt(HttpServletRequest request) {
        // 1. Tentar cookie HttpOnly primeiro (JavaScript não consegue ler — XSS-safe)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (accessCookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && !value.isBlank()) {
                        return value;
                    }
                }
            }
        }

        // 2. Fallback: Authorization Bearer (suporte a sessões antigas durante migração)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        return null;
    }

    private boolean isTenantExempt(HttpServletRequest request) {
        String path = request.getServletPath();
        return TENANT_EXEMPT_PREFIXES.stream().anyMatch(path::startsWith);
    }
}

