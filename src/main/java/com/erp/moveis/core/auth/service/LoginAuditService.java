package com.erp.moveis.core.auth.service;

import com.erp.moveis.core.audit.AuditLog;
import com.erp.moveis.core.audit.AuditLogRepository;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serviço de auditoria de autenticação.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Registrar tentativas de login (sucesso e falha) na tabela audit_logs</li>
 *   <li>Incrementar contador de falhas consecutivas no usuário</li>
 *   <li>Bloquear conta após {@value #MAX_ATTEMPTS} falhas por {@value #LOCKOUT_MINUTES} minutos</li>
 *   <li>Resetar contador e desbloqueio após login bem-sucedido</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAuditService {

    static final int MAX_ATTEMPTS = 5;
    static final int LOCKOUT_MINUTES = 15;

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    /**
     * Verifica se a conta está temporariamente bloqueada ANTES de tentar autenticar.
     * Lança 429 Too Many Requests se bloqueada.
     */
    public void checkNotLocked(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
                log.warn("[LOGIN_AUDIT] Tentativa em conta bloqueada: username={}", username);
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Conta bloqueada temporariamente. Tente novamente em " + LOCKOUT_MINUTES + " minutos.");
            }
        });
    }

    /**
     * Registra falha de login: incrementa contador e bloqueia a conta se atingir o limite.
     * Executa em nova transação para persistir mesmo que a transação pai faça rollback.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String username, String ip) {
        Optional<User> maybeUser = userRepository.findByUsername(username);

        if (maybeUser.isPresent()) {
            User user = maybeUser.get();
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            String details;
            if (attempts >= MAX_ATTEMPTS) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCKOUT_MINUTES));
                details = "Conta bloqueada por " + LOCKOUT_MINUTES + "min após " + MAX_ATTEMPTS + " tentativas falhas";
                log.warn("[LOGIN_AUDIT] Conta bloqueada: username={}, ip={}", username, ip);
                saveAuditLog(user, ip, "LOGIN_LOCKED", details);
            } else {
                details = "Tentativa " + attempts + "/" + MAX_ATTEMPTS;
                log.warn("[LOGIN_AUDIT] Falha de login ({}/{}): username={}, ip={}", attempts, MAX_ATTEMPTS, username, ip);
                saveAuditLog(user, ip, "LOGIN_FAILED", details);
            }

            userRepository.save(user);
        } else {
            // Usuário não encontrado — registramos sem expor detalhes ao chamador
            log.warn("[LOGIN_AUDIT] Login com usuário inexistente: username={}, ip={}", username, ip);
            saveAuditLogAnonymous(username, ip, "LOGIN_FAILED", "Usuário não encontrado");
        }
    }

    /**
     * Registra sucesso de login: zera o contador de falhas e remove o bloqueio.
     * Executa em nova transação independente.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordSuccess(User user, String ip) {
        if (user.getFailedLoginAttempts() > 0 || user.getLockedUntil() != null) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }
        saveAuditLog(user, ip, "LOGIN_SUCCESS", null);
        log.info("[LOGIN_AUDIT] Login bem-sucedido: username={}, ip={}", user.getUsername(), ip);
    }

    private void saveAuditLog(User user, String ip, String action, String details) {
        AuditLog entry = AuditLog.builder()
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .userId(user.getId())
                .username(user.getUsername())
                .action(action)
                .entityType("AUTH")
                .ipAddress(ip)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }

    private void saveAuditLogAnonymous(String username, String ip, String action, String details) {
        AuditLog entry = AuditLog.builder()
                .username(username)
                .action(action)
                .entityType("AUTH")
                .ipAddress(ip)
                .details(details)
                .build();
        auditLogRepository.save(entry);
    }
}
