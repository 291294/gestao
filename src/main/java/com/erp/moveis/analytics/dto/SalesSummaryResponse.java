package com.erp.moveis.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SalesSummaryResponse {

    private Long totalOrders;

    private Double totalRevenue;
}
