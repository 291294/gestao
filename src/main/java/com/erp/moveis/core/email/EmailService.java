package com.erp.moveis.core.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("[EMAIL_SENT] to={} subject={}", to, subject);
        } catch (Exception e) {
            log.error("[EMAIL_FAILED] to={} error={}", to, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmation(String to, String orderNumber, Double total) {
        String subject = "Confirmação de Pedido - " + orderNumber;
        String body = String.format(
                "Seu pedido %s foi confirmado!\n\nValor total: R$ %.2f\n\nObrigado pela preferência.",
                orderNumber, total
        );
        sendSimpleEmail(to, subject, body);
    }

    @Async
    public void sendInvoiceNotification(String to, String invoiceNumber) {
        String subject = "Fatura Emitida - " + invoiceNumber;
        String body = String.format(
                "A fatura %s foi emitida e está disponível para pagamento.\n\nAtenciosamente,\nERP Móveis",
                invoiceNumber
        );
        sendSimpleEmail(to, subject, body);
    }
}
