package com.erp.moveis.inventory.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.dto.InventoryItemRequest;
import com.erp.moveis.inventory.dto.InventoryItemResponse;
import com.erp.moveis.inventory.dto.InventoryMovementResponse;
import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.entity.InventoryMovement;
import com.erp.moveis.inventory.entity.MovementType;
import com.erp.moveis.inventory.mapper.InventoryMapper;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import com.erp.moveis.inventory.repository.InventoryMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryMovementRepository movementRepository;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public InventoryItemResponse createItem(InventoryItemRequest request) {
        itemRepository.findByCompanyIdAndProductId(request.getCompanyId(), request.getProductId())
                .ifPresent(existing -> {
                    throw new BusinessException("Inventory item already exists for product " + request.getProductId());
                });
        InventoryItem item = InventoryMapper.toEntity(request);
        return InventoryMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getItem(Long id) {
        return InventoryMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryItemResponse getByProduct(Long companyId, Long productId) {
        InventoryItem item = itemRepository.findByCompanyIdAndProductId(companyId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem for product " + productId, companyId));
        return InventoryMapper.toResponse(item);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getByCompany(Long companyId) {
        return itemRepository.findByCompanyId(companyId).stream()
                .map(InventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStock(Long companyId) {
        return itemRepository.findLowStock(companyId).stream()
                .map(InventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getOutOfStock(Long companyId) {
        return itemRepository.findOutOfStock(companyId).stream()
                .map(InventoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Movimentações ──────────────────────────────────────────

    @Override
    @Transactional
    public InventoryMovementResponse addStock(Long itemId, int quantity, String referenceType, Long referenceId, String notes) {
        if (quantity <= 0) throw new BusinessException("Quantity must be positive");
        InventoryItem item = findEntityById(itemId);
        int previous = item.getQuantityOnHand();
        item.setQuantityOnHand(previous + quantity);
        item.setLastRestockDate(LocalDate.now());
        itemRepository.save(item);

        return InventoryMapper.toMovementResponse(
                createMovement(item, MovementType.IN, quantity, previous, item.getQuantityOnHand(), referenceType, referenceId, notes)
        );
    }

    @Override
    @Transactional
    public InventoryMovementResponse removeStock(Long itemId, int quantity, String referenceType, Long referenceId, String notes) {
        if (quantity <= 0) throw new BusinessException("Quantity must be positive");
        InventoryItem item = findEntityById(itemId);
        if (item.getQuantityAvailable() < quantity) {
            throw new BusinessException("Insufficient stock. Available: " + item.getQuantityAvailable() + ", requested: " + quantity);
        }
        int previous = item.getQuantityOnHand();
        item.setQuantityOnHand(previous - quantity);
        itemRepository.save(item);

        return InventoryMapper.toMovementResponse(
                createMovement(item, MovementType.OUT, quantity, previous, item.getQuantityOnHand(), referenceType, referenceId, notes)
        );
    }

    @Override
    @Transactional
    public InventoryMovementResponse adjustStock(Long itemId, int newQuantity, String notes) {
        if (newQuantity < 0) throw new BusinessException("Quantity cannot be negative");
        InventoryItem item = findEntityById(itemId);
        int previous = item.getQuantityOnHand();
        int diff = newQuantity - previous;
        item.setQuantityOnHand(newQuantity);
        itemRepository.save(item);

        return InventoryMapper.toMovementResponse(
                createMovement(item, MovementType.ADJUSTMENT, Math.abs(diff), previous, newQuantity, "ADJUSTMENT", null, notes)
        );
    }

    @Override
    @Transactional
    public InventoryItemResponse reserveStock(Long itemId, int quantity) {
        if (quantity <= 0) throw new BusinessException("Quantity must be positive");
        InventoryItem item = findEntityById(itemId);
        if (item.getQuantityAvailable() < quantity) {
            throw new BusinessException("Insufficient available stock for reservation");
        }
        item.setQuantityReserved(item.getQuantityReserved() + quantity);
        return InventoryMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public InventoryItemResponse releaseReservation(Long itemId, int quantity) {
        if (quantity <= 0) throw new BusinessException("Quantity must be positive");
        InventoryItem item = findEntityById(itemId);
        int newReserved = item.getQuantityReserved() - quantity;
        item.setQuantityReserved(Math.max(0, newReserved));
        return InventoryMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getMovements(Long itemId) {
        return movementRepository.findByInventoryItemIdOrderByCreatedAtDesc(itemId).stream()
                .map(InventoryMapper::toMovementResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────

    private InventoryItem findEntityById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", id));
    }

    private InventoryMovement createMovement(InventoryItem item, MovementType type, int qty,
                                              int previous, int newQty,
                                              String refType, Long refId, String notes) {
        InventoryMovement movement = InventoryMovement.builder()
                .companyId(item.getCompanyId())
                .inventoryItemId(item.getId())
                .movementType(type)
                .quantity(qty)
                .previousQuantity(previous)
                .newQuantity(newQty)
                .referenceType(refType)
                .referenceId(refId)
                .notes(notes)
                .build();
        return movementRepository.save(movement);
    }
}
