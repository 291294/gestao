package com.erp.moveis.analytics.service;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;
import com.erp.moveis.analytics.repository.AnalyticsRepository;
import com.erp.moveis.dto.DashboardResponse;
import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import com.erp.moveis.manufacturing.entity.ProductionOrder;
import com.erp.moveis.manufacturing.entity.ProductionStatus;
import com.erp.moveis.manufacturing.repository.ProductionOrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplTest {

    @Mock private AnalyticsRepository repository;
    @Mock private ProductionOrderRepository productionOrderRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @InjectMocks private AnalyticsServiceImpl service;

    @Test @DisplayName("getSalesSummary — should return orders count and revenue")
    void shouldGetSalesSummary() {
        when(repository.countOrders(1L)).thenReturn(50L);
        when(repository.totalRevenue(1L)).thenReturn(150000.0);

        SalesSummaryResponse result = service.getSalesSummary(1L);
        assertThat(result.getTotalOrders()).isEqualTo(50L);
        assertThat(result.getTotalRevenue()).isEqualTo(150000.0);
    }

    @Test @DisplayName("getRevenueSummary — should return total revenue")
    void shouldGetRevenueSummary() {
        when(repository.totalRevenue(1L)).thenReturn(200000.0);

        RevenueSummaryResponse result = service.getRevenueSummary(1L);
        assertThat(result.getTotalRevenue()).isEqualTo(200000.0);
    }

    @Test @DisplayName("getDashboard — should aggregate all stats")
    void shouldGetDashboard() {
        when(repository.countOrders(1L)).thenReturn(100L);
        when(repository.totalRevenue(1L)).thenReturn(500000.0);
        when(repository.countClients()).thenReturn(25L);
        when(repository.revenueByMonth(1L)).thenReturn(Collections.emptyList());
        when(repository.countOrdersByStatus(1L)).thenReturn(Collections.emptyList());

        ProductionOrder activeProd = new ProductionOrder();
        activeProd.setStatus(ProductionStatus.IN_PROGRESS);
        when(productionOrderRepository.findByCompanyId(1L)).thenReturn(List.of(activeProd));

        InventoryItem lowItem = new InventoryItem();
        lowItem.setQuantityOnHand(2);
        lowItem.setMinStockLevel(10);
        when(inventoryItemRepository.findByCompanyId(1L)).thenReturn(List.of(lowItem));

        DashboardResponse result = service.getDashboard(1L);
        assertThat(result.getStats().getTotalOrders()).isEqualTo(100L);
        assertThat(result.getStats().getTotalRevenue()).isEqualTo(500000.0);
        assertThat(result.getStats().getActiveProduction()).isEqualTo(1L);
        assertThat(result.getStats().getLowStockItems()).isEqualTo(1L);
    }

    @Test @DisplayName("getDashboard — should handle null values gracefully")
    void shouldHandleNullValues() {
        when(repository.countOrders(1L)).thenReturn(null);
        when(repository.totalRevenue(1L)).thenReturn(null);
        when(repository.countClients()).thenReturn(null);
        when(repository.revenueByMonth(1L)).thenReturn(Collections.emptyList());
        when(repository.countOrdersByStatus(1L)).thenReturn(Collections.emptyList());
        when(productionOrderRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());
        when(inventoryItemRepository.findByCompanyId(1L)).thenReturn(Collections.emptyList());

        DashboardResponse result = service.getDashboard(1L);
        assertThat(result.getStats().getTotalOrders()).isEqualTo(0L);
        assertThat(result.getStats().getTotalRevenue()).isEqualTo(0.0);
    }
}
