package com.erp.moveis.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItemRequest {
    private Long companyId;
    private Long productId;
    private String warehouseLocation;
    private Integer quantityOnHand;
    private Integer minStockLevel;
    private Integer maxStockLevel;
    private BigDecimal unitCost;
}
