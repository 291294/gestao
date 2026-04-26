package com.erp.moveis.core.auth.service;

import com.erp.moveis.core.auth.entity.RefreshToken;
import com.erp.moveis.core.auth.repository.RefreshTokenRepository;
import com.erp.moveis.core.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    @Transactional
    public RefreshToken create(User user) {
        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpirationMs / 1000));
        token.setRevoked(false);
        return repository.save(token);
    }

    /**
     * Valida, revoga o token atual e cria um novo (rotação).
     * Se o token já estiver revogado, todos os tokens do usuário são revogados
     * (detecção de replay attack / token roubado).
     */
    @Transactional
    public RefreshToken rotate(String tokenValue) {
        RefreshToken existing = repository.findByToken(tokenValue)
                .orElseThrow(() -> {
                    log.warn("[REFRESH_TOKEN] Token não encontrado: {}", tokenValue);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token inválido");
                });

        if (existing.isRevoked()) {
            // Possível replay attack: revogar TODOS os tokens do usuário
            log.warn("[REPLAY_ATTACK] Token revogado reutilizado para usuário id={} — revogando todos os tokens",
                    existing.getUser().getId());
            repository.revokeAllByUserId(existing.getUser().getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revogado");
        }

        if (existing.isExpired()) {
            log.info("[REFRESH_TOKEN] Token expirado para usuário id={}", existing.getUser().getId());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expirado");
        }

        // Revogar o token atual e registrar o substituto
        existing.setRevoked(true);
        RefreshToken newToken = create(existing.getUser());
        existing.setReplacedBy(newToken.getToken());
        repository.save(existing);

        log.debug("[REFRESH_TOKEN] Rotação bem-sucedida para usuário id={}", existing.getUser().getId());
        return newToken;
    }

    /**
     * Revoga todos os tokens ativos de um usuário.
     * Chamado em logout e troca de senha.
     */
    @Transactional
    public void revokeAllByUser(Long userId) {
        repository.revokeAllByUserId(userId);
        log.info("[REFRESH_TOKEN] Todos os tokens revogados para usuário id={}", userId);
    }
}
