package com.erp.moveis.manufacturing.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.service.InventoryService;
import com.erp.moveis.manufacturing.entity.*;
import com.erp.moveis.manufacturing.repository.BillOfMaterialRepository;
import com.erp.moveis.manufacturing.repository.ProductionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ManufacturingServiceImplTest {

    @Mock private ProductionOrderRepository orderRepository;
    @Mock private BillOfMaterialRepository bomRepository;
    @Mock private InventoryService inventoryService;
    @InjectMocks private ManufacturingServiceImpl service;

    private ProductionOrder prodOrder;
    private BillOfMaterial bom;

    @BeforeEach
    void setUp() {
        prodOrder = new ProductionOrder();
        prodOrder.setId(1L);
        prodOrder.setCompanyId(1L);
        prodOrder.setProductId(10L);
        prodOrder.setQuantity(new BigDecimal("5"));
        prodOrder.setStatus(ProductionStatus.CREATED);

        BillOfMaterialItem bomItem = new BillOfMaterialItem();
        bomItem.setMaterialProductId(20L);
        bomItem.setQuantity(new BigDecimal("2"));

        bom = new BillOfMaterial();
        bom.setProductId(10L);
        bom.setItems(List.of(bomItem));
    }

    @Test @DisplayName("createProductionOrder — should create with CREATED status")
    void shouldCreateProductionOrder() {
        when(orderRepository.save(any(ProductionOrder.class))).thenAnswer(inv -> {
            ProductionOrder o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        ProductionOrder result = service.createProductionOrder(1L, 10L, new BigDecimal("5"));
        assertThat(result.getStatus()).isEqualTo(ProductionStatus.CREATED);
        assertThat(result.getCompanyId()).isEqualTo(1L);
        verify(orderRepository).save(any(ProductionOrder.class));
    }

    @Test @DisplayName("findByCompany — should return orders for company")
    void shouldFindByCompany() {
        when(orderRepository.findByCompanyId(1L)).thenReturn(List.of(prodOrder));
        List<ProductionOrder> result = service.findByCompany(1L);
        assertThat(result).hasSize(1);
    }

    @Test @DisplayName("findByCompany(null) — should return all orders")
    void shouldReturnAllWhenCompanyNull() {
        when(orderRepository.findAll()).thenReturn(List.of(prodOrder));
        List<ProductionOrder> result = service.findByCompany(null);
        assertThat(result).hasSize(1);
        verify(orderRepository).findAll();
    }

    @Test @DisplayName("startProduction — should consume BOM materials and set IN_PROGRESS")
    void shouldStartProduction() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(prodOrder));
        when(bomRepository.findByProductId(10L)).thenReturn(bom);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.startProduction(1L);

        assertThat(prodOrder.getStatus()).isEqualTo(ProductionStatus.IN_PROGRESS);
        verify(inventoryService).removeWarehouseStock(eq(1L), eq(20L), eq(1L), eq(new BigDecimal("10")));
    }

    @Test @DisplayName("startProduction — should throw when status is not CREATED")
    void shouldThrowWhenStatusNotCreated() {
        prodOrder.setStatus(ProductionStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(prodOrder));

        assertThatThrownBy(() -> service.startProduction(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CREATED");
    }

    @Test @DisplayName("startProduction — should throw when no BOM found")
    void shouldThrowWhenNoBom() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(prodOrder));
        when(bomRepository.findByProductId(10L)).thenReturn(null);

        assertThatThrownBy(() -> service.startProduction(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bill of Materials");
    }

    @Test @DisplayName("finishProduction — should add stock and set FINISHED")
    void shouldFinishProduction() {
        prodOrder.setStatus(ProductionStatus.IN_PROGRESS);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(prodOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.finishProduction(1L);

        assertThat(prodOrder.getStatus()).isEqualTo(ProductionStatus.FINISHED);
        verify(inventoryService).addWarehouseStock(eq(1L), eq(10L), eq(1L), eq(new BigDecimal("5")));
    }

    @Test @DisplayName("finishProduction — should throw when not IN_PROGRESS")
    void shouldThrowWhenNotInProgress() {
        prodOrder.setStatus(ProductionStatus.CREATED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(prodOrder));

        assertThatThrownBy(() -> service.finishProduction(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("IN_PROGRESS");
    }
}
