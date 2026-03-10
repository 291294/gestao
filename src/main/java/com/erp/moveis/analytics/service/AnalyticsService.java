package com.erp.moveis.analytics.service;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;
import com.erp.moveis.dto.DashboardResponse;

public interface AnalyticsService {

    SalesSummaryResponse getSalesSummary(Long companyId);

    RevenueSummaryResponse getRevenueSummary(Long companyId);

    DashboardResponse getDashboard(Long companyId);
}
