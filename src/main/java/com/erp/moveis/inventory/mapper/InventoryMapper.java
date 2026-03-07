package com.erp.moveis.inventory.mapper;

import com.erp.moveis.inventory.dto.*;
import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.entity.InventoryMovement;

public class InventoryMapper {

    private InventoryMapper() {}

    public static InventoryItemResponse toResponse(InventoryItem item) {
        InventoryItemResponse dto = new InventoryItemResponse();
        dto.setId(item.getId());
        dto.setCompanyId(item.getCompanyId());
        dto.setProductId(item.getProductId());
        dto.setWarehouseLocation(item.getWarehouseLocation());
        dto.setQuantityOnHand(item.getQuantityOnHand());
        dto.setQuantityReserved(item.getQuantityReserved());
        dto.setQuantityAvailable(item.getQuantityAvailable());
        dto.setMinStockLevel(item.getMinStockLevel());
        dto.setMaxStockLevel(item.getMaxStockLevel());
        dto.setUnitCost(item.getUnitCost());
        dto.setLastRestockDate(item.getLastRestockDate());
        dto.setLowStock(item.isLowStock());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static InventoryItem toEntity(InventoryItemRequest request) {
        InventoryItem item = new InventoryItem();
        item.setCompanyId(request.getCompanyId());
        item.setProductId(request.getProductId());
        item.setWarehouseLocation(request.getWarehouseLocation());
        item.setQuantityOnHand(request.getQuantityOnHand());
        item.setMinStockLevel(request.getMinStockLevel());
        item.setMaxStockLevel(request.getMaxStockLevel());
        item.setUnitCost(request.getUnitCost());
        return item;
    }

    public static InventoryMovementResponse toMovementResponse(InventoryMovement mov) {
        InventoryMovementResponse dto = new InventoryMovementResponse();
        dto.setId(mov.getId());
        dto.setCompanyId(mov.getCompanyId());
        dto.setInventoryItemId(mov.getInventoryItemId());
        dto.setMovementType(mov.getMovementType());
        dto.setQuantity(mov.getQuantity());
        dto.setPreviousQuantity(mov.getPreviousQuantity());
        dto.setNewQuantity(mov.getNewQuantity());
        dto.setReferenceType(mov.getReferenceType());
        dto.setReferenceId(mov.getReferenceId());
        dto.setNotes(mov.getNotes());
        dto.setCreatedBy(mov.getCreatedBy());
        dto.setCreatedAt(mov.getCreatedAt());
        return dto;
    }
}
