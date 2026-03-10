package com.erp.moveis.promob.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromobItemResponse {

    private Long id;
    private String name;
    private String description;
    private Long productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal depth;
}
