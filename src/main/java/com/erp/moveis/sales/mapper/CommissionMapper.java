package com.erp.moveis.sales.mapper;

import com.erp.moveis.sales.dto.CommissionRequest;
import com.erp.moveis.sales.dto.CommissionResponse;
import com.erp.moveis.sales.entity.Commission;

public class CommissionMapper {

    private CommissionMapper() {}

    // ── Entity → Response ──────────────────────────────────────

    public static CommissionResponse toResponse(Commission commission) {
        CommissionResponse dto = new CommissionResponse();
        dto.setId(commission.getId());
        dto.setCompanyId(commission.getCompanyId());
        dto.setSellerId(commission.getSellerId());
        dto.setOrderId(commission.getOrderId());
        dto.setQuoteId(commission.getQuoteId());
        dto.setCommissionPercentage(commission.getCommissionPercentage());
        dto.setSaleAmount(commission.getSaleAmount());
        dto.setCommissionAmount(commission.getCommissionAmount());
        dto.setStatus(commission.getStatus());
        dto.setPaymentDate(commission.getPaymentDate());
        dto.setNotes(commission.getNotes());
        dto.setCreatedAt(commission.getCreatedAt());
        dto.setUpdatedAt(commission.getUpdatedAt());
        return dto;
    }

    // ── Request → Entity ──────────────────────────────────────

    public static Commission toEntity(CommissionRequest request) {
        Commission commission = new Commission();
        commission.setCompanyId(request.getCompanyId());
        commission.setSellerId(request.getSellerId());
        commission.setOrderId(request.getOrderId());
        commission.setQuoteId(request.getQuoteId());
        commission.setCommissionPercentage(request.getCommissionPercentage());
        commission.setSaleAmount(request.getSaleAmount());
        commission.setPaymentDate(request.getPaymentDate());
        commission.setNotes(request.getNotes());
        return commission;
    }
}
