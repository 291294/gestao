package com.erp.moveis.core.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Enfileira mensagens de email no Redis (lista LPUSH).
 * O consumidor {@link EmailQueueConsumer} processa via @Scheduled.
 * Garante que emails sobrevivem a reinicializações da aplicação.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    static final String QUEUE_KEY = "erp:email:queue";

    private final RedisTemplate<String, EmailMessage> emailRedisTemplate;

    public void enqueue(String to, String subject, String body) {
        EmailMessage message = new EmailMessage(to, subject, body);
        emailRedisTemplate.opsForList().leftPush(QUEUE_KEY, message);
        log.debug("[EMAIL_ENQUEUED] to={} subject={}", to, subject);
    }

    public void enqueueOrderConfirmation(String to, String orderNumber, Double total) {
        enqueue(
                to,
                "Confirmação de Pedido - " + orderNumber,
                String.format("Seu pedido %s foi confirmado!%n%nValor total: R$ %.2f%n%nObrigado pela preferência.",
                        orderNumber, total)
        );
    }

    public void enqueueInvoiceNotification(String to, String invoiceNumber) {
        enqueue(
                to,
                "Fatura Emitida - " + invoiceNumber,
                String.format("A fatura %s foi emitida e está disponível para pagamento.%n%nAtenciosamente,%nERP Móveis",
                        invoiceNumber)
        );
    }
}
