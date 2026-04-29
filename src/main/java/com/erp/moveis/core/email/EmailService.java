package com.erp.moveis.core.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Fachada de envio de email.
 * Delega para {@link EmailQueueService} que persiste na fila Redis.
 * O processamento real ocorre em {@link EmailQueueConsumer} a cada 5 segundos.
 * Emails sobrevivem a reinicializações da aplicação.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final EmailQueueService emailQueueService;

    public void sendSimpleEmail(String to, String subject, String body) {
        emailQueueService.enqueue(to, subject, body);
    }

    public void sendOrderConfirmation(String to, String orderNumber, Double total) {
        emailQueueService.enqueueOrderConfirmation(to, orderNumber, total);
    }

    public void sendInvoiceNotification(String to, String invoiceNumber) {
        emailQueueService.enqueueInvoiceNotification(to, invoiceNumber);
    }
}

