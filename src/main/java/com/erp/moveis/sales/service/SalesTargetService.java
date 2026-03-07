package com.erp.moveis.sales.service;

import com.erp.moveis.sales.dto.SalesTargetRequest;
import com.erp.moveis.sales.dto.SalesTargetResponse;
import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;

import java.math.BigDecimal;
import java.util.List;

public interface SalesTargetService {

    SalesTargetResponse createTarget(SalesTargetRequest request);

    SalesTargetResponse getTarget(Long id);

    List<SalesTargetResponse> getTargetsBySeller(Long sellerId, TargetStatus status);

    List<SalesTargetResponse> getTargetsByCompany(Long companyId, TargetType type);

    List<SalesTargetResponse> getActiveTargetsForSeller(Long sellerId);

    SalesTargetResponse addAchievedAmount(Long targetId, BigDecimal saleAmount);

    void updateSellerTargets(Long sellerId, BigDecimal saleAmount);

    SalesTargetResponse complete(Long id);

    SalesTargetResponse cancel(Long id);

    void closeExpiredTargets();
}
