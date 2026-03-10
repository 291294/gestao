package com.erp.moveis.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductionOrderResponse {
    private Long id;
    private Long companyId;
    private Long productId;
    private BigDecimal quantity;
    private String status;
    private LocalDateTime createdAt;
}
