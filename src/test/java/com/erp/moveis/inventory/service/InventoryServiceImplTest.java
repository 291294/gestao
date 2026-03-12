package com.erp.moveis.inventory.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.dto.InventoryItemRequest;
import com.erp.moveis.inventory.dto.InventoryItemResponse;
import com.erp.moveis.inventory.dto.InventoryMovementResponse;
import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.entity.InventoryMovement;
import com.erp.moveis.inventory.entity.MovementType;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import com.erp.moveis.inventory.repository.InventoryMovementRepository;
import com.erp.moveis.inventory.repository.StockMovementRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl — Unit Tests")
class InventoryServiceImplTest {

    @Mock
    private InventoryItemRepository itemRepository;

    @Mock
    private InventoryMovementRepository movementRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private InventoryItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = InventoryItem.builder()
                .id(1L)
                .companyId(1L)
                .productId(100L)
                .warehouseLocation("A-01")
                .quantityOnHand(50)
                .quantityReserved(10)
                .minStockLevel(5)
                .maxStockLevel(200)
                .unitCost(new BigDecimal("25.00"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createItem ─────────────────────────────────────────────

    @Test
    @DisplayName("createItem — should create inventory item successfully")
    void createItem_success() {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setCompanyId(1L);
        request.setProductId(100L);
        request.setWarehouseLocation("A-01");
        request.setQuantityOnHand(50);
        request.setMinStockLevel(5);
        request.setMaxStockLevel(200);
        request.setUnitCost(new BigDecimal("25.00"));

        when(itemRepository.findByCompanyIdAndProductId(1L, 100L)).thenReturn(Optional.empty());
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        InventoryItemResponse response = inventoryService.createItem(request);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(100L);
        assertThat(response.getQuantityOnHand()).isEqualTo(50);
        verify(itemRepository).save(any(InventoryItem.class));
    }

    @Test
    @DisplayName("createItem — should throw when item already exists")
    void createItem_duplicate() {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setCompanyId(1L);
        request.setProductId(100L);

        when(itemRepository.findByCompanyIdAndProductId(1L, 100L)).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> inventoryService.createItem(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    // ── addStock ───────────────────────────────────────────────

    @Test
    @DisplayName("addStock — should increment quantity and create IN movement")
    void addStock_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        InventoryMovement savedMovement = InventoryMovement.builder()
                .id(1L).companyId(1L).inventoryItemId(1L)
                .movementType(MovementType.IN)
                .quantity(20).previousQuantity(50).newQuantity(70)
                .referenceType("PURCHASE").notes("Restock")
                .createdAt(LocalDateTime.now())
                .build();
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse response = inventoryService.addStock(1L, 20, "PURCHASE", 1L, "Restock");

        assertThat(response).isNotNull();
        assertThat(response.getMovementType()).isEqualTo(MovementType.IN);
        assertThat(response.getQuantity()).isEqualTo(20);
        verify(itemRepository).save(argThat(item -> item.getQuantityOnHand() == 70));
    }

    @Test
    @DisplayName("addStock — should throw when quantity is zero")
    void addStock_zeroQuantity() {
        assertThatThrownBy(() -> inventoryService.addStock(1L, 0, "PURCHASE", 1L, "note"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("positive");
    }

    @Test
    @DisplayName("addStock — should throw when quantity is negative")
    void addStock_negativeQuantity() {
        assertThatThrownBy(() -> inventoryService.addStock(1L, -5, "PURCHASE", 1L, "note"))
                .isInstanceOf(BusinessException.class);
    }

    // ── removeStock ────────────────────────────────────────────

    @Test
    @DisplayName("removeStock — should decrement quantity and create OUT movement")
    void removeStock_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        InventoryMovement savedMovement = InventoryMovement.builder()
                .id(2L).companyId(1L).inventoryItemId(1L)
                .movementType(MovementType.OUT)
                .quantity(10).previousQuantity(50).newQuantity(40)
                .createdAt(LocalDateTime.now())
                .build();
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse response = inventoryService.removeStock(1L, 10, "SALE", 1L, "Venda");

        assertThat(response).isNotNull();
        assertThat(response.getMovementType()).isEqualTo(MovementType.OUT);
        verify(itemRepository).save(argThat(item -> item.getQuantityOnHand() == 40));
    }

    @Test
    @DisplayName("removeStock — should throw when insufficient stock")
    void removeStock_insufficientStock() {
        // available = 50 - 10 = 40
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> inventoryService.removeStock(1L, 50, "SALE", 1L, "Big sale"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient");
    }

    // ── adjustStock ────────────────────────────────────────────

    @Test
    @DisplayName("adjustStock — should set exact quantity")
    void adjustStock_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        InventoryMovement savedMovement = InventoryMovement.builder()
                .id(3L).companyId(1L).inventoryItemId(1L)
                .movementType(MovementType.ADJUSTMENT)
                .quantity(30).previousQuantity(50).newQuantity(80)
                .createdAt(LocalDateTime.now())
                .build();
        when(movementRepository.save(any(InventoryMovement.class))).thenReturn(savedMovement);

        InventoryMovementResponse response = inventoryService.adjustStock(1L, 80, "Ajuste inventário");

        assertThat(response).isNotNull();
        assertThat(response.getMovementType()).isEqualTo(MovementType.ADJUSTMENT);
        verify(itemRepository).save(argThat(item -> item.getQuantityOnHand() == 80));
    }

    @Test
    @DisplayName("adjustStock — should throw when negative quantity")
    void adjustStock_negative() {
        assertThatThrownBy(() -> inventoryService.adjustStock(1L, -1, "Invalid"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("negative");
    }

    // ── reserveStock ───────────────────────────────────────────

    @Test
    @DisplayName("reserveStock — should increment reserved quantity")
    void reserveStock_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        inventoryService.reserveStock(1L, 10);

        verify(itemRepository).save(argThat(item -> item.getQuantityReserved() == 20));
    }

    @Test
    @DisplayName("reserveStock — should throw when insufficient available stock")
    void reserveStock_insufficient() {
        // available = 50 - 10 = 40
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> inventoryService.reserveStock(1L, 50))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient");
    }

    // ── releaseReservation ─────────────────────────────────────

    @Test
    @DisplayName("releaseReservation — should decrement reserved quantity")
    void releaseReservation_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        inventoryService.releaseReservation(1L, 5);

        verify(itemRepository).save(argThat(item -> item.getQuantityReserved() == 5));
    }

    @Test
    @DisplayName("releaseReservation — should not go below zero")
    void releaseReservation_belowZero() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(itemRepository.save(any(InventoryItem.class))).thenReturn(sampleItem);

        inventoryService.releaseReservation(1L, 100);

        verify(itemRepository).save(argThat(item -> item.getQuantityReserved() == 0));
    }

    // ── getItem / getByCompany ─────────────────────────────────

    @Test
    @DisplayName("getItem — should return item by id")
    void getItem_success() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        InventoryItemResponse response = inventoryService.getItem(1L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("getItem — should throw when not found")
    void getItem_notFound() {
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getItem(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByCompany — should return items for company")
    void getByCompany_success() {
        when(itemRepository.findByCompanyId(1L)).thenReturn(List.of(sampleItem));

        List<InventoryItemResponse> result = inventoryService.getByCompany(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCompanyId()).isEqualTo(1L);
    }

    // ── Warehouse-aware stock ──────────────────────────────────

    @Test
    @DisplayName("addWarehouseStock — should save movement with positive quantity")
    void addWarehouseStock_success() {
        inventoryService.addWarehouseStock(1L, 100L, 1L, new BigDecimal("50"));

        verify(stockMovementRepository).save(argThat(m ->
                m.getQuantity().compareTo(new BigDecimal("50")) == 0
                        && m.getProductId() == 100L
        ));
    }

    @Test
    @DisplayName("addWarehouseStock — should throw when quantity is zero")
    void addWarehouseStock_zero() {
        assertThatThrownBy(() -> inventoryService.addWarehouseStock(1L, 100L, 1L, BigDecimal.ZERO))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("removeWarehouseStock — should throw when insufficient stock")
    void removeWarehouseStock_insufficient() {
        when(stockMovementRepository.getCurrentStock(100L, 1L)).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> inventoryService.removeWarehouseStock(1L, 100L, 1L, new BigDecimal("10")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient");
    }

    @Test
    @DisplayName("transferStock — should create two movements (out + in)")
    void transferStock_success() {
        when(stockMovementRepository.getCurrentStock(100L, 1L)).thenReturn(new BigDecimal("50"));

        inventoryService.transferStock(1L, 100L, 1L, 2L, new BigDecimal("20"));

        verify(stockMovementRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("transferStock — should throw when insufficient source stock")
    void transferStock_insufficient() {
        when(stockMovementRepository.getCurrentStock(100L, 1L)).thenReturn(new BigDecimal("5"));

        assertThatThrownBy(() -> inventoryService.transferStock(1L, 100L, 1L, 2L, new BigDecimal("20")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient");
    }
}
