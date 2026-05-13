package com.erp.moveis.core.auth.dto;

import java.util.List;

/**
 * Resposta do endpoint GET /auth/me.
 * Contém apenas dados do usuário — sem tokens — para uso no frontend
 * ao verificar a sessão ativa via cookie HttpOnly.
 */
public record UserInfoResponse(
        String username,
        String email,
        String fullName,
        Long companyId,
        List<String> roles,
        List<String> permissions
) {}
