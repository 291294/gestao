package com.erp.moveis.inventory.dto;

import com.erp.moveis.inventory.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementRequest {
    private Long companyId;
    private Long inventoryItemId;
    private MovementType movementType;
    private Integer quantity;
    private String referenceType;
    private Long referenceId;
    private String notes;
}
