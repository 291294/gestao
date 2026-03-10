package com.erp.moveis.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private DashboardStatsResponse stats;
    private List<RevenueByMonthResponse> revenueByMonth;
    private List<OrdersByStatusResponse> ordersByStatus;
}
