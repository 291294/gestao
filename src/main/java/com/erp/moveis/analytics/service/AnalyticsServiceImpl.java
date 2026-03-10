package com.erp.moveis.analytics.service;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;
import com.erp.moveis.analytics.repository.AnalyticsRepository;
import com.erp.moveis.dto.*;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import com.erp.moveis.manufacturing.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository repository;
    private final ProductionOrderRepository productionOrderRepository;
    private final InventoryItemRepository inventoryItemRepository;

    @Override
    public SalesSummaryResponse getSalesSummary(Long companyId) {
        Long orders = repository.countOrders(companyId);
        Double revenue = repository.totalRevenue(companyId);

        return SalesSummaryResponse.builder()
                .totalOrders(orders)
                .totalRevenue(revenue)
                .build();
    }

    @Override
    public RevenueSummaryResponse getRevenueSummary(Long companyId) {
        Double revenue = repository.totalRevenue(companyId);

        return RevenueSummaryResponse.builder()
                .totalRevenue(revenue)
                .build();
    }

    @Override
    public DashboardResponse getDashboard(Long companyId) {
        Long totalOrders = repository.countOrders(companyId);
        Double totalRevenue = repository.totalRevenue(companyId);
        Long totalClients = repository.countClients();

        long activeProduction = productionOrderRepository.findByCompanyId(companyId)
                .stream().filter(p -> p.getStatus().name().equals("IN_PROGRESS")).count();

        long lowStockItems = inventoryItemRepository.findByCompanyId(companyId)
                .stream().filter(i -> i.getQuantityOnHand() != null && i.getMinStockLevel() != null
                        && i.getQuantityOnHand() <= i.getMinStockLevel()).count();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .totalOrders(totalOrders != null ? totalOrders : 0)
                .totalClients(totalClients != null ? totalClients : 0)
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .activeProduction(activeProduction)
                .lowStockItems(lowStockItems)
                .build();

        // Revenue by month
        List<RevenueByMonthResponse> revenueByMonth = new ArrayList<>();
        try {
            List<Object[]> monthData = repository.revenueByMonth(companyId);
            for (Object[] row : monthData) {
                String month = (String) row[0];
                double rev = ((Number) row[1]).doubleValue();
                revenueByMonth.add(new RevenueByMonthResponse(month, rev));
            }
            Collections.reverse(revenueByMonth);
        } catch (Exception ignored) {}

        // Orders by status
        List<OrdersByStatusResponse> ordersByStatus = new ArrayList<>();
        try {
            List<Object[]> statusData = repository.countOrdersByStatus(companyId);
            for (Object[] row : statusData) {
                String status = (String) row[0];
                long count = ((Number) row[1]).longValue();
                ordersByStatus.add(new OrdersByStatusResponse(status != null ? status : "N/A", count));
            }
        } catch (Exception ignored) {}

        return DashboardResponse.builder()
                .stats(stats)
                .revenueByMonth(revenueByMonth)
                .ordersByStatus(ordersByStatus)
                .build();
    }
}
