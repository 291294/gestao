package com.erp.moveis.core.auth.controller;

import com.erp.moveis.core.auth.dto.LoginRequest;
import com.erp.moveis.core.auth.dto.RegisterCompanyRequest;
import com.erp.moveis.core.auth.dto.RegisterRequest;
import com.erp.moveis.core.auth.dto.TokenResponse;
import com.erp.moveis.core.auth.dto.UserInfoResponse;
import com.erp.moveis.core.auth.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Controller de autenticação com suporte a HttpOnly Cookies (modo seguro) e
 * Authorization Bearer (compatibilidade durante migração gradual).
 *
 * <p><b>Estratégia de cookies:</b></p>
 * <ul>
 *   <li>{@code erp_access_token} — acesso geral, Path=/, MaxAge=jwtExpiration</li>
 *   <li>{@code erp_refresh_token} — renovação, Path=/api/auth/refresh, MaxAge=refreshExpiration</li>
 * </ul>
 *
 * <p><b>Migração gradual:</b> O JSON de login ainda retorna os tokens para não quebrar
 * sessões de clientes que ainda lêem do localStorage. Quando todos os clientes estiverem
 * atualizados para usar cookies, remova {@code accessToken} e {@code refreshToken} do
 * {@link TokenResponse} enviado ao cliente (mantenha os cookies).</p>
 *
 * <p><b>Atributos dos cookies:</b></p>
 * <ul>
 *   <li>{@code HttpOnly=true} — JavaScript não consegue ler (proteção XSS)</li>
 *   <li>{@code Secure=true} em produção — apenas HTTPS (definir COOKIE_SECURE=true)</li>
 *   <li>{@code SameSite=Lax} — bloqueia POST cross-site (mitigação CSRF sem CSRF tokens)</li>
 *   <li>{@code Path} restrito para refresh — cookie de refresh enviado apenas ao endpoint correto</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${cookie.access.name:erp_access_token}")
    private String accessCookieName;

    @Value("${cookie.refresh.name:erp_refresh_token}")
    private String refreshCookieName;

    @Value("${cookie.access.secure:false}")
    private boolean cookieSecure;

    @Value("${cookie.refresh.path:/api/auth/refresh}")
    private String refreshCookiePath;

    @Value("${jwt.expiration:900000}")
    private long jwtExpiration;

    @Value("${jwt.refresh.expiration:604800000}")
    private long refreshExpiration;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // =========================================================================
    // Login
    // =========================================================================

    /**
     * Autentica o usuário e define cookies HttpOnly de acesso e refresh.
     *
     * <p>O JSON de resposta ainda inclui os tokens para compatibilidade com clientes legados
     * (localStorage). Após a migração completa do frontend, os tokens podem ser omitidos
     * do JSON — os cookies serão suficientes.</p>
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.authenticate(request);
        setAuthCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================================
    // Registro
    // =========================================================================

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.register(request);
        setAuthCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/register-company")
    public ResponseEntity<TokenResponse> registerCompany(
            @Valid @RequestBody RegisterCompanyRequest request,
            HttpServletResponse response) {

        TokenResponse tokenResponse = authService.registerCompany(request);
        setAuthCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================================
    // Renovação de token (Refresh)
    // =========================================================================

    /**
     * Rotaciona o par de tokens (access + refresh).
     *
     * <p>Aceita o refresh token via cookie HttpOnly (modo cookie) OU via header
     * {@code Authorization: Bearer} (modo legado). Cookie tem prioridade.</p>
     *
     * <p>Após renovação bem-sucedida, novos cookies são definidos e o JSON ainda
     * retorna os tokens para clientes legados.</p>
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Cookie tem prioridade sobre o header (modo seguro)
        String refreshToken = extractCookie(request, refreshCookieName);

        // Fallback: Authorization Bearer (clientes legados com localStorage)
        if (refreshToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            refreshToken = authHeader.substring(7);
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        TokenResponse tokenResponse = authService.refreshToken(refreshToken);
        setAuthCookies(response, tokenResponse);
        return ResponseEntity.ok(tokenResponse);
    }

    // =========================================================================
    // Logout
    // =========================================================================

    /**
     * Invalida a sessão do usuário: revoga todos os refresh tokens no banco e
     * expira os cookies no browser (MaxAge=0).
     *
     * <p>Exige acesso autenticado (cookie ou Bearer header válido).</p>
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletResponse response) {

        authService.logout(userDetails.getUsername());
        clearAuthCookies(response);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // Verificação de sessão ativa (/me)
    // =========================================================================

    /**
     * Retorna os dados do usuário autenticado para verificação de sessão.
     *
     * <p>O frontend chama este endpoint ao inicializar para verificar se há uma sessão
     * ativa via cookie. Retorna 200 com dados do usuário se autenticado, ou 401 se
     * o cookie estiver ausente ou expirado.</p>
     *
     * <p>Não retorna tokens — apenas metadados do usuário.</p>
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> me(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserInfoResponse info = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(info);
    }

    // =========================================================================
    // Helpers privados
    // =========================================================================

    /**
     * Define os cookies de acesso e refresh na resposta HTTP.
     * Ambos com HttpOnly=true, SameSite=Lax e path apropriado.
     */
    private void setAuthCookies(HttpServletResponse response, TokenResponse tokenResponse) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildAccessCookie(tokenResponse.getAccessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildRefreshCookie(tokenResponse.getRefreshToken()).toString());
    }

    /**
     * Expira os cookies de acesso e refresh (MaxAge=0) para efetuar logout no browser.
     */
    private void clearAuthCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildExpiredCookie(accessCookieName, "/").toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                buildExpiredCookie(refreshCookieName, refreshCookiePath).toString());
    }

    private ResponseCookie buildAccessCookie(String token) {
        return ResponseCookie.from(accessCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofMillis(jwtExpiration))
                .build();
    }

    private ResponseCookie buildRefreshCookie(String token) {
        return ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(refreshCookiePath)
                .maxAge(Duration.ofMillis(refreshExpiration))
                .build();
    }

    private ResponseCookie buildExpiredCookie(String name, String path) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path(path)
                .maxAge(0)
                .build();
    }

    /**
     * Lê o valor de um cookie pelo nome a partir da requisição HTTP.
     * Retorna {@code null} se o cookie não existir ou estiver vazio.
     */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                String value = cookie.getValue();
                return (value != null && !value.isBlank()) ? value : null;
            }
        }
        return null;
    }
}

