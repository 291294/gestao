package com.erp.moveis.finance.service;

import com.erp.moveis.finance.dto.PaymentRequest;
import com.erp.moveis.finance.dto.PaymentResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPayment(Long id);

    List<PaymentResponse> getByInvoice(Long invoiceId);

    List<PaymentResponse> getByCompany(Long companyId);

    PaymentResponse confirm(Long id);

    PaymentResponse cancel(Long id);

    PaymentResponse refund(Long id);

    BigDecimal getTotalConfirmedByInvoice(Long invoiceId);

    BigDecimal getRevenueByPeriod(Long companyId, LocalDateTime start, LocalDateTime end);

    List<PaymentResponse> getByCompanyAndPeriod(Long companyId, LocalDateTime start, LocalDateTime end);
}
