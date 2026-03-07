package com.erp.moveis.invoicing.service;

import com.erp.moveis.invoicing.dto.InvoiceItemRequest;
import com.erp.moveis.invoicing.dto.InvoiceRequest;
import com.erp.moveis.invoicing.dto.InvoiceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InvoiceService {

    InvoiceResponse createInvoice(InvoiceRequest request);

    InvoiceResponse getInvoice(Long id);

    Page<InvoiceResponse> getByCompany(Long companyId, Pageable pageable);

    List<InvoiceResponse> getByClient(Long clientId);

    InvoiceResponse issue(Long id);

    InvoiceResponse send(Long id);

    InvoiceResponse cancel(Long id);

    InvoiceResponse registerPayment(Long id, BigDecimal amount);

    InvoiceResponse addItem(Long invoiceId, InvoiceItemRequest itemRequest);

    InvoiceResponse calculateTotals(Long id);

    List<InvoiceResponse> getOverdue();

    BigDecimal getOpenBalanceByClient(Long clientId);

    BigDecimal getTotalRevenue(Long companyId);

    InvoiceResponse createFromOrder(Long orderId, Long companyId, Long clientId);
}
