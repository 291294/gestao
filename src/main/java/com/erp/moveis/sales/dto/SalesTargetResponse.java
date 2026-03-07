package com.erp.moveis.sales.dto;

import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTargetResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private Long sellerId;
    private String sellerName;
    private TargetType targetType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal targetAmount;
    private BigDecimal achievedAmount;
    private BigDecimal achievementPercentage;
    private TargetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
