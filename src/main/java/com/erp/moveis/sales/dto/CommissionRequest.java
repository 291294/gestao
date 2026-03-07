package com.erp.moveis.sales.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRequest {
    private Long companyId;
    private Long sellerId;
    private Long orderId;
    private Long quoteId;
    private BigDecimal commissionPercentage;
    private BigDecimal saleAmount;
    private LocalDate paymentDate;
    private String notes;
}
