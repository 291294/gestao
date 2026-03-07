package com.erp.moveis.sales.dto;

import com.erp.moveis.sales.model.SalesTarget.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTargetRequest {
    private Long companyId;
    private Long sellerId;
    private TargetType targetType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal targetAmount;
}
