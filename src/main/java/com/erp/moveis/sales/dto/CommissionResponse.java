package com.erp.moveis.sales.dto;

import com.erp.moveis.sales.model.Commission.CommissionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommissionResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long sellerId;
    private String sellerName;
    private Long orderId;
    private String orderNumber;
    private Long quoteId;
    private String quoteNumber;
    private BigDecimal commissionPercentage;
    private BigDecimal saleAmount;
    private BigDecimal commissionAmount;
    private CommissionStatus status;
    private LocalDate paymentDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
