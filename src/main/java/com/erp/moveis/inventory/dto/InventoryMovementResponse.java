package com.erp.moveis.inventory.dto;

import com.erp.moveis.inventory.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponse {
    private Long id;
    private Long companyId;
    private Long inventoryItemId;
    private MovementType movementType;
    private Integer quantity;
    private Integer previousQuantity;
    private Integer newQuantity;
    private String referenceType;
    private Long referenceId;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
}
