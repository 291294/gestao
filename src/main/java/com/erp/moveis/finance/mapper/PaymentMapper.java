package com.erp.moveis.finance.mapper;

import com.erp.moveis.finance.dto.PaymentRequest;
import com.erp.moveis.finance.dto.PaymentResponse;
import com.erp.moveis.finance.entity.Payment;
import com.erp.moveis.finance.entity.PaymentMethod;
import com.erp.moveis.finance.entity.PaymentStatus;

public class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .companyId(payment.getCompanyId())
                .invoiceId(payment.getInvoiceId())
                .paymentNumber(payment.getPaymentNumber())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                .paymentDate(payment.getPaymentDate())
                .confirmationDate(payment.getConfirmationDate())
                .transactionId(payment.getTransactionId())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    public static Payment toEntity(PaymentRequest request) {
        return Payment.builder()
                .companyId(request.getCompanyId())
                .invoiceId(request.getInvoiceId())
                .amount(request.getAmount())
                .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                .transactionId(request.getTransactionId())
                .notes(request.getNotes())
                .build();
    }
}
