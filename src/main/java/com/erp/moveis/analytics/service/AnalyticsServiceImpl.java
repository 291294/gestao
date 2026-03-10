package com.erp.moveis.analytics.service;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;
import com.erp.moveis.analytics.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository repository;

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
}
