package com.erp.moveis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    @NotNull(message = "productId é obrigatório")
    private Long productId;
    @NotNull @Positive
    private BigDecimal quantity;
    @NotNull @Positive
    private BigDecimal unitPrice;
    private String notes;
}
