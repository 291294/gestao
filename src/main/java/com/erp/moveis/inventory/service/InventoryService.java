package com.erp.moveis.inventory.service;

import com.erp.moveis.inventory.dto.InventoryItemRequest;
import com.erp.moveis.inventory.dto.InventoryItemResponse;
import com.erp.moveis.inventory.dto.InventoryMovementResponse;
import com.erp.moveis.inventory.entity.MovementType;

import java.util.List;

public interface InventoryService {

    InventoryItemResponse createItem(InventoryItemRequest request);

    InventoryItemResponse getItem(Long id);

    InventoryItemResponse getByProduct(Long companyId, Long productId);

    List<InventoryItemResponse> getByCompany(Long companyId);

    List<InventoryItemResponse> getLowStock(Long companyId);

    List<InventoryItemResponse> getOutOfStock(Long companyId);

    InventoryMovementResponse addStock(Long itemId, int quantity, String referenceType, Long referenceId, String notes);

    InventoryMovementResponse removeStock(Long itemId, int quantity, String referenceType, Long referenceId, String notes);

    InventoryMovementResponse adjustStock(Long itemId, int newQuantity, String notes);

    InventoryItemResponse reserveStock(Long itemId, int quantity);

    InventoryItemResponse releaseReservation(Long itemId, int quantity);

    List<InventoryMovementResponse> getMovements(Long itemId);
}
