package com.erp.moveis.promob.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromobCutlistResponse {

    private Long id;
    private String partName;
    private String material;
    private BigDecimal thickness;
    private BigDecimal width;
    private BigDecimal height;
    private Integer quantity;
    private Boolean edgeTop;
    private Boolean edgeBottom;
    private Boolean edgeLeft;
    private Boolean edgeRight;
    private String notes;
    private Long productionOrderId;
}
