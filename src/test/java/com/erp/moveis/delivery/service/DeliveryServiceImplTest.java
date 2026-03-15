package com.erp.moveis.delivery.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.delivery.dto.DeliveryItemRequest;
import com.erp.moveis.delivery.dto.DeliveryRequest;
import com.erp.moveis.delivery.dto.DeliveryResponse;
import com.erp.moveis.delivery.entity.Delivery;
import com.erp.moveis.delivery.entity.DeliveryItem;
import com.erp.moveis.delivery.entity.DeliveryStatus;
import com.erp.moveis.delivery.mapper.DeliveryMapper;
import com.erp.moveis.delivery.repository.DeliveryRepository;
import com.erp.moveis.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private InventoryService inventoryService;
    @InjectMocks private DeliveryServiceImpl deliveryService;

    private Delivery sampleDelivery;

    @BeforeEach
    void setUp() {
        sampleDelivery = new Delivery();
        sampleDelivery.setId(1L);
        sampleDelivery.setCompanyId(1L);
        sampleDelivery.setOrderId(100L);
        sampleDelivery.setDeliveryNumber("ENT-2026-ABC12345");
        sampleDelivery.setStatus(DeliveryStatus.PENDING);
        sampleDelivery.setDeliveryAddress("Rua Teste, 123");
        sampleDelivery.setItems(new ArrayList<>());
    }

    // ── GET ────────────────────────────────────────────────

    @Test
    @DisplayName("getDelivery — should return delivery by ID")
    void shouldGetDeliveryById() {
        when(deliveryRepository.findFullDelivery(1L)).thenReturn(Optional.of(sampleDelivery));

        DeliveryResponse response = deliveryService.getDelivery(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getDelivery — should throw 404 for non-existent delivery")
    void shouldThrowWhenDeliveryNotFound() {
        when(deliveryRepository.findFullDelivery(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deliveryService.getDelivery(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByOrder — should return deliveries for order")
    void shouldGetByOrder() {
        when(deliveryRepository.findByOrderId(100L)).thenReturn(List.of(sampleDelivery));

        List<DeliveryResponse> result = deliveryService.getByOrder(100L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByCompanyAndStatus — should filter by status")
    void shouldGetByCompanyAndStatus() {
        when(deliveryRepository.findByCompanyIdAndStatus(1L, DeliveryStatus.PENDING))
                .thenReturn(List.of(sampleDelivery));

        List<DeliveryResponse> result = deliveryService.getByCompanyAndStatus(1L, DeliveryStatus.PENDING);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getInTransit — should return in-transit deliveries")
    void shouldGetInTransit() {
        sampleDelivery.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findInTransit(1L)).thenReturn(List.of(sampleDelivery));

        List<DeliveryResponse> result = deliveryService.getInTransit(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getScheduledForDate — should return scheduled deliveries")
    void shouldGetScheduledForDate() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        when(deliveryRepository.findScheduledForDate(date)).thenReturn(List.of(sampleDelivery));

        List<DeliveryResponse> result = deliveryService.getScheduledForDate(date);

        assertThat(result).hasSize(1);
    }

    // ── SHIP ───────────────────────────────────────────────

    @Test
    @DisplayName("ship — should ship a pending delivery and deduct stock")
    void shouldShipPendingDelivery() {
        DeliveryItem item = new DeliveryItem();
        item.setProductId(5L);
        item.setQuantity(2);
        sampleDelivery.getItems().add(item);

        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.ship(1L);

        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.IN_TRANSIT);
        verify(inventoryService).removeStockByProduct(eq(1L), eq(5L), eq(2), eq("DELIVERY"), eq(1L), any());
        verify(inventoryService).removeWarehouseStock(eq(1L), eq(5L), eq(1L), any());
    }

    @Test
    @DisplayName("ship — should reject shipping a delivered delivery")
    void shouldRejectShipDelivered() {
        sampleDelivery.setStatus(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        assertThatThrownBy(() -> deliveryService.ship(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING or PREPARING");
    }

    // ── DELIVER ────────────────────────────────────────────

    @Test
    @DisplayName("deliver — should mark in-transit delivery as delivered")
    void shouldDeliverInTransit() {
        sampleDelivery.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.deliver(1L);

        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    @DisplayName("deliver — should reject delivering a pending delivery")
    void shouldRejectDeliverPending() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        assertThatThrownBy(() -> deliveryService.deliver(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("IN_TRANSIT");
    }

    // ── CANCEL ─────────────────────────────────────────────

    @Test
    @DisplayName("cancel — should cancel a pending delivery")
    void shouldCancelPendingDelivery() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryResponse response = deliveryService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo(DeliveryStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel — should reject cancelling a delivered delivery")
    void shouldRejectCancelDelivered() {
        sampleDelivery.setStatus(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        assertThatThrownBy(() -> deliveryService.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("delivered");
    }

    // ── ADD ITEM ───────────────────────────────────────────

    @Test
    @DisplayName("addItem — should add item to pending delivery")
    void shouldAddItemToPending() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(inv -> inv.getArgument(0));

        DeliveryItemRequest itemReq = new DeliveryItemRequest();
        itemReq.setProductId(5L);
        itemReq.setQuantity(3);

        DeliveryResponse response = deliveryService.addItem(1L, itemReq);

        assertThat(response).isNotNull();
        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    @DisplayName("addItem — should reject adding item to non-pending delivery")
    void shouldRejectAddItemToShipped() {
        sampleDelivery.setStatus(DeliveryStatus.IN_TRANSIT);
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(sampleDelivery));

        DeliveryItemRequest itemReq = new DeliveryItemRequest();
        itemReq.setProductId(5L);
        itemReq.setQuantity(3);

        assertThatThrownBy(() -> deliveryService.addItem(1L, itemReq))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }
}
