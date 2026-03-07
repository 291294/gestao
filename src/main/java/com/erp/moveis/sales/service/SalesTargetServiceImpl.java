package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.sales.dto.SalesTargetRequest;
import com.erp.moveis.sales.dto.SalesTargetResponse;
import com.erp.moveis.sales.entity.SalesTarget;
import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;
import com.erp.moveis.sales.mapper.SalesTargetMapper;
import com.erp.moveis.sales.repository.SalesTargetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesTargetServiceImpl implements SalesTargetService {

    private final SalesTargetRepository salesTargetRepository;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public SalesTargetResponse createTarget(SalesTargetRequest request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new BusinessException("Period end must be after period start");
        }
        SalesTarget target = SalesTargetMapper.toEntity(request);
        SalesTarget saved = salesTargetRepository.save(target);
        return SalesTargetMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SalesTargetResponse getTarget(Long id) {
        SalesTarget target = findEntityById(id);
        return SalesTargetMapper.toResponse(target);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesTargetResponse> getTargetsBySeller(Long sellerId, TargetStatus status) {
        return salesTargetRepository.findBySellerIdAndStatus(sellerId, status).stream()
                .map(SalesTargetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesTargetResponse> getTargetsByCompany(Long companyId, TargetType type) {
        return salesTargetRepository.findByCompanyIdAndTargetType(companyId, type).stream()
                .map(SalesTargetMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SalesTargetResponse> getActiveTargetsForSeller(Long sellerId) {
        return salesTargetRepository.findActiveTargetsForSellerAtDate(sellerId, LocalDate.now()).stream()
                .map(SalesTargetMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Atualização de progresso ───────────────────────────────

    @Override
    @Transactional
    public SalesTargetResponse addAchievedAmount(Long targetId, BigDecimal saleAmount) {
        SalesTarget target = findEntityById(targetId);
        if (target.getStatus() != TargetStatus.ACTIVE) {
            throw new BusinessException("Can only update ACTIVE targets. Current: " + target.getStatus());
        }
        BigDecimal current = target.getAchievedAmount() != null ? target.getAchievedAmount() : BigDecimal.ZERO;
        target.setAchievedAmount(current.add(saleAmount));
        target.calculateAchievement();

        // auto-complete se atingiu 100%
        if (target.getAchievementPercentage().compareTo(new BigDecimal("100")) >= 0) {
            target.setStatus(TargetStatus.COMPLETED);
        }

        return SalesTargetMapper.toResponse(salesTargetRepository.save(target));
    }

    @Override
    @Transactional
    public void updateSellerTargets(Long sellerId, BigDecimal saleAmount) {
        List<SalesTarget> activeTargets = salesTargetRepository
                .findActiveTargetsForSellerAtDate(sellerId, LocalDate.now());

        for (SalesTarget target : activeTargets) {
            BigDecimal current = target.getAchievedAmount() != null ? target.getAchievedAmount() : BigDecimal.ZERO;
            target.setAchievedAmount(current.add(saleAmount));
            target.calculateAchievement();

            if (target.getAchievementPercentage().compareTo(new BigDecimal("100")) >= 0) {
                target.setStatus(TargetStatus.COMPLETED);
            }

            salesTargetRepository.save(target);
        }
    }

    // ── Workflow ────────────────────────────────────────────────

    @Override
    @Transactional
    public SalesTargetResponse complete(Long id) {
        SalesTarget target = findEntityById(id);
        if (target.getStatus() != TargetStatus.ACTIVE) {
            throw new BusinessException("Only ACTIVE targets can be completed. Current: " + target.getStatus());
        }
        target.setStatus(TargetStatus.COMPLETED);
        return SalesTargetMapper.toResponse(salesTargetRepository.save(target));
    }

    @Override
    @Transactional
    public SalesTargetResponse cancel(Long id) {
        SalesTarget target = findEntityById(id);
        if (target.getStatus() == TargetStatus.COMPLETED) {
            throw new BusinessException("Cannot cancel a completed target");
        }
        target.setStatus(TargetStatus.CANCELLED);
        return SalesTargetMapper.toResponse(salesTargetRepository.save(target));
    }

    @Override
    @Transactional
    public void closeExpiredTargets() {
        List<SalesTarget> expired = salesTargetRepository.findExpiredTargets(LocalDate.now());
        for (SalesTarget target : expired) {
            target.setStatus(TargetStatus.COMPLETED);
            salesTargetRepository.save(target);
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private SalesTarget findEntityById(Long id) {
        return salesTargetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SalesTarget", id));
    }
}
