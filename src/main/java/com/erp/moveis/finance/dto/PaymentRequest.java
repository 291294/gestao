package com.erp.moveis.finance.dto;

import java.math.BigDecimal;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    private Long companyId;
    private Long invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String notes;
}
