package com.erp.moveis.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.inventory.service.InventoryService;
import com.erp.moveis.model.Order;
import com.erp.moveis.model.OrderItem;
import com.erp.moveis.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository repository;
    @Mock private InventoryService inventoryService;
    @InjectMocks private OrderService service;

    private Order order;
    private OrderItem item;

    @BeforeEach
    void setUp() {
        TenantContext.setTenantId(1L);

        item = new OrderItem();
        item.setProductId(10L);
        item.setQuantity(new BigDecimal("3"));
        item.setUnitPrice(new BigDecimal("500"));

        order = new Order();
        order.setId(1L);
        order.setCompanyId(1L);
        order.setStatus("PENDING");
        order.setTotalValue(new BigDecimal("1500"));
        order.setItems(new ArrayList<>(List.of(item)));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test @DisplayName("list — should return all orders for tenant")
    void shouldListAll() {
        when(repository.findByCompanyId(1L)).thenReturn(List.of(order));
        assertThat(service.list()).hasSize(1);
    }

    @Test @DisplayName("listPaged — should return paged orders for tenant")
    void shouldListPaged() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(repository.findByCompanyId(1L, pageable)).thenReturn(new PageImpl<>(List.of(order)));
        Page<Order> result = service.listPaged(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test @DisplayName("findById — should return order for tenant")
    void shouldFindById() {
        when(repository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(order));
        assertThat(service.findById(1L)).isPresent();
    }

    @Test @DisplayName("findByClientId — should return client orders for tenant")
    void shouldFindByClientId() {
        when(repository.findByCompanyIdAndClientId(1L, 5L)).thenReturn(List.of(order));
        assertThat(service.findByClientId(5L)).hasSize(1);
    }

    @Test @DisplayName("save — should calculate totals and reserve stock")
    void shouldSaveWithStockReservation() {
        when(repository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        Order result = service.save(order);
        assertThat(result.getId()).isEqualTo(1L);
        verify(inventoryService).reserveWarehouseStock(eq(1L), eq(10L), eq(1L), eq(new BigDecimal("3")), eq(1L));
    }

    @Test @DisplayName("delete — should delete order for tenant")
    void shouldDelete() {
        when(repository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(order));
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test @DisplayName("update — should update order fields")
    void shouldUpdate() {
        when(repository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order updates = new Order();
        updates.setStatus("CONFIRMED");
        updates.setTotalValue(new BigDecimal("2000"));

        Order result = service.update(1L, updates);
        assertThat(result.getStatus()).isEqualTo("CONFIRMED");
        assertThat(result.getTotalValue()).isEqualTo(new BigDecimal("2000"));
    }

    @Test @DisplayName("cancel — should release stock and set CANCELLED")
    void shouldCancelAndReleaseStock() {
        when(repository.findByIdAndCompanyId(1L, 1L)).thenReturn(Optional.of(order));
        when(repository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order result = service.cancel(1L);
        assertThat(result.getStatus()).isEqualTo("CANCELLED");
        verify(inventoryService).releaseWarehouseReservation(eq(1L), eq(10L), eq(1L), eq(new BigDecimal("3")), eq(1L));
    }
}
