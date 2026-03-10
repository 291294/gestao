package com.erp.moveis.invoicing.scheduler;

import com.erp.moveis.invoicing.entity.Invoice;
import com.erp.moveis.invoicing.entity.InvoiceStatus;
import com.erp.moveis.invoicing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {

    private final InvoiceRepository invoiceRepository;

    /**
     * Verifica faturas vencidas a cada hora e marca como OVERDUE.
     * Critério: due_date < hoje AND status IN (ISSUED, SENT, PARTIALLY_PAID)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void markOverdueInvoices() {
        List<Invoice> overdue = invoiceRepository.findOverdueInvoices();

        if (overdue.isEmpty()) {
            return;
        }

        int count = 0;
        for (Invoice invoice : overdue) {
            if (invoice.getStatus() == InvoiceStatus.ISSUED
                    || invoice.getStatus() == InvoiceStatus.SENT
                    || invoice.getStatus() == InvoiceStatus.PARTIALLY_PAID) {

                invoice.setStatus(InvoiceStatus.OVERDUE);
                count++;

                log.info("[INVOICE_OVERDUE] Invoice {} marked as OVERDUE (due: {}, total: {}, paid: {})",
                        invoice.getInvoiceNumber(),
                        invoice.getDueDate(),
                        invoice.getTotalAmount(),
                        invoice.getAmountPaid());
            }
        }

        if (count > 0) {
            invoiceRepository.saveAll(overdue);
            log.info("[INVOICE_OVERDUE] {} invoice(s) marked as OVERDUE", count);
        }
    }
}
