package com.erp.moveis.inventory.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.dto.InventoryItemRequest;
import com.erp.moveis.inventory.dto.InventoryItemResponse;
import com.erp.moveis.inventory.dto.InventoryMovementResponse;
import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.entity.InventoryMovement;
import com.erp.moveis.inventory.entity.MovementType;
import com.erp.moveis.inventory.entity.StockMovement;
import com.erp.moveis.inventory.entity.StockMovementType;
import com.erp.moveis.inventory.mapper.InventoryMapper;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import com.erp.moveis.inventory.repository.InventoryMovementRepository;
import com.erp.moveis.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryMovementRepository movementRepository;
    private final StockMovementRepository stockMovementRepository;

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

    @Override
    @Transactional
    public InventoryItemResponse updateItem(Long id, InventoryItemRequest request) {
        InventoryItem item = findEntityById(id);
        if (request.getWarehouseLocation() != null) item.setWarehouseLocation(request.getWarehouseLocation());
        if (request.getMinStockLevel() != null) item.setMinStockLevel(request.getMinStockLevel());
        if (request.getMaxStockLevel() != null) item.setMaxStockLevel(request.getMaxStockLevel());
        if (request.getUnitCost() != null) item.setUnitCost(request.getUnitCost());
        return InventoryMapper.toResponse(itemRepository.save(item));
    }

    @Override
    @Transactional
    public InventoryMovementResponse removeStockByProduct(Long companyId, Long productId, int quantity,
                                                           String referenceType, Long referenceId, String notes) {
        InventoryItem item = itemRepository.findByCompanyIdAndProductId(companyId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InventoryItem for company " + companyId + " and product " + productId, companyId));
        return removeStock(item.getId(), quantity, referenceType, referenceId, notes);
    }

    // ── Warehouse-aware stock movements ─────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWarehouseStock(Long productId, Long warehouseId) {
        return stockMovementRepository.getCurrentStock(productId, warehouseId);
    }

    @Override
    @Transactional
    public void addWarehouseStock(Long companyId, Long productId, Long warehouseId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        StockMovement movement = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .movementType(StockMovementType.PURCHASE)
                .quantity(quantity)
                .build();
        stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public void removeWarehouseStock(Long companyId, Long productId, Long warehouseId, BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        BigDecimal currentStock = stockMovementRepository.getCurrentStock(productId, warehouseId);
        if (currentStock.compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient warehouse stock. Available: " + currentStock + ", requested: " + quantity);
        }
        StockMovement movement = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .movementType(StockMovementType.SALE)
                .quantity(quantity.negate())
                .build();
        stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public void reserveWarehouseStock(Long companyId, Long productId, Long warehouseId,
                                       BigDecimal quantity, Long orderId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        BigDecimal currentStock = stockMovementRepository.getCurrentStock(productId, warehouseId);
        if (currentStock.compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient stock for product " + productId
                    + ". Available: " + currentStock + ", requested: " + quantity);
        }
        StockMovement movement = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .movementType(StockMovementType.RESERVATION)
                .quantity(quantity.negate())
                .referenceType("ORDER")
                .referenceId(orderId)
                .build();
        stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public void releaseWarehouseReservation(Long companyId, Long productId, Long warehouseId,
                                             BigDecimal quantity, Long orderId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        StockMovement movement = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(warehouseId)
                .movementType(StockMovementType.RELEASE)
                .quantity(quantity)
                .referenceType("ORDER_CANCEL")
                .referenceId(orderId)
                .build();
        stockMovementRepository.save(movement);
    }

    @Override
    @Transactional
    public void transferStock(Long companyId, Long productId,
                              Long fromWarehouse, Long toWarehouse,
                              BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Quantity must be positive");
        }
        BigDecimal stock = stockMovementRepository.getCurrentStock(productId, fromWarehouse);
        if (stock.compareTo(quantity) < 0) {
            throw new BusinessException("Insufficient stock for transfer. Available: " + stock + ", requested: " + quantity);
        }

        StockMovement out = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(fromWarehouse)
                .movementType(StockMovementType.TRANSFER)
                .quantity(quantity.negate())
                .referenceType("TRANSFER_OUT")
                .build();

        StockMovement in = StockMovement.builder()
                .companyId(companyId)
                .productId(productId)
                .warehouseId(toWarehouse)
                .movementType(StockMovementType.TRANSFER)
                .quantity(quantity)
                .referenceType("TRANSFER_IN")
                .build();

        stockMovementRepository.save(out);
        stockMovementRepository.save(in);
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
