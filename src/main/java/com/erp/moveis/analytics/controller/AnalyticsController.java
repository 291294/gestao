package com.erp.moveis.analytics.controller;

import com.erp.moveis.analytics.dto.RevenueSummaryResponse;
import com.erp.moveis.analytics.dto.SalesSummaryResponse;
import com.erp.moveis.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService service;

    @GetMapping("/sales-summary")
    public SalesSummaryResponse salesSummary(@RequestParam Long companyId) {
        return service.getSalesSummary(companyId);
    }

    @GetMapping("/revenue-summary")
    public RevenueSummaryResponse revenueSummary(@RequestParam Long companyId) {
        return service.getRevenueSummary(companyId);
    }
}
