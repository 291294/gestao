package com.erp.moveis.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String notes;
}
