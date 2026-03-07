package com.erp.moveis.sales.mapper;

import com.erp.moveis.sales.dto.SalesTargetRequest;
import com.erp.moveis.sales.dto.SalesTargetResponse;
import com.erp.moveis.sales.entity.SalesTarget;

public class SalesTargetMapper {

    private SalesTargetMapper() {}

    // ── Entity → Response ──────────────────────────────────────

    public static SalesTargetResponse toResponse(SalesTarget target) {
        SalesTargetResponse dto = new SalesTargetResponse();
        dto.setId(target.getId());
        dto.setCompanyId(target.getCompanyId());
        dto.setSellerId(target.getSellerId());
        dto.setTargetType(target.getTargetType());
        dto.setPeriodStart(target.getPeriodStart());
        dto.setPeriodEnd(target.getPeriodEnd());
        dto.setTargetAmount(target.getTargetAmount());
        dto.setAchievedAmount(target.getAchievedAmount());
        dto.setAchievementPercentage(target.getAchievementPercentage());
        dto.setStatus(target.getStatus());
        dto.setCreatedAt(target.getCreatedAt());
        dto.setUpdatedAt(target.getUpdatedAt());
        return dto;
    }

    // ── Request → Entity ──────────────────────────────────────

    public static SalesTarget toEntity(SalesTargetRequest request) {
        SalesTarget target = new SalesTarget();
        target.setCompanyId(request.getCompanyId());
        target.setSellerId(request.getSellerId());
        target.setTargetType(request.getTargetType());
        target.setPeriodStart(request.getPeriodStart());
        target.setPeriodEnd(request.getPeriodEnd());
        target.setTargetAmount(request.getTargetAmount());
        return target;
    }
}
