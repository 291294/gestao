package com.erp.moveis.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalOrders;
    private long totalClients;
    private double totalRevenue;
    private long lowStockItems;
    private long activeProduction;
    private long totalInvoices;
    private long overdueInvoices;
}
