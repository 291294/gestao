package com.erp.moveis.core.email;

import java.io.Serializable;

/**
 * Mensagem de email serializada para a fila Redis.
 * Imutável, serializável via Jackson.
 */
public record EmailMessage(
        String to,
        String subject,
        String body
) implements Serializable {}
