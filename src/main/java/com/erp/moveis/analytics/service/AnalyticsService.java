package com.erp.moveis.analytics.service;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;

public interface AnalyticsService {

    SalesSummaryResponse getSalesSummary(Long companyId);

    RevenueSummaryResponse getRevenueSummary(Long companyId);
}
