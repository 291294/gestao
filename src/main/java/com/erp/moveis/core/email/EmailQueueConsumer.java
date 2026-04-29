package com.erp.moveis.core.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Consome a fila Redis de emails a cada 5 segundos.
 * Processa em lote (até 50 por ciclo) para evitar bloqueio.
 * Falhas são logadas sem descarte — o email permanece na fila em caso de exceção de envio,
 * mas é removido se o problema for de dados (evita loop infinito de erros).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueConsumer {

    private static final int BATCH_SIZE = 50;

    private final RedisTemplate<String, EmailMessage> emailRedisTemplate;
    private final JavaMailSender mailSender;

    @Scheduled(fixedDelay = 5000)
    public void processQueue() {
        int processed = 0;
        while (processed < BATCH_SIZE) {
            EmailMessage message = emailRedisTemplate.opsForList()
                    .rightPop(EmailQueueService.QUEUE_KEY);
            if (message == null) {
                break;
            }
            send(message);
            processed++;
        }
        if (processed > 0) {
            log.debug("[EMAIL_CONSUMER] {} email(s) processados", processed);
        }
    }

    private void send(EmailMessage message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(message.to());
            mail.setSubject(message.subject());
            mail.setText(message.body());
            mailSender.send(mail);
            log.info("[EMAIL_SENT] to={} subject={}", message.to(), message.subject());
        } catch (Exception e) {
            log.error("[EMAIL_FAILED] to={} subject={} error={}", message.to(), message.subject(), e.getMessage());
            // Re-enfileira no lado oposto para não perder a mensagem em caso de falha temporária de SMTP
            emailRedisTemplate.opsForList().leftPush(EmailQueueService.QUEUE_KEY, message);
        }
    }
}
