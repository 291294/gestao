package com.erp.moveis.finance.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long companyId;
    private Long invoiceId;
    private String paymentNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String status;
    private LocalDateTime paymentDate;
    private LocalDateTime confirmationDate;
    private String transactionId;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
