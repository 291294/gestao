package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.sales.dto.CommissionRequest;
import com.erp.moveis.sales.dto.CommissionResponse;
import com.erp.moveis.sales.entity.Commission;
import com.erp.moveis.sales.entity.Commission.CommissionStatus;
import com.erp.moveis.sales.mapper.CommissionMapper;
import com.erp.moveis.sales.repository.CommissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final CommissionRepository commissionRepository;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public CommissionResponse createCommission(CommissionRequest request) {
        Commission commission = CommissionMapper.toEntity(request);
        commission.calculateCommission();
        Commission saved = commissionRepository.save(commission);
        return CommissionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CommissionResponse getCommission(Long id) {
        Commission commission = findEntityById(id);
        return CommissionMapper.toResponse(commission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsBySeller(Long sellerId) {
        return commissionRepository.findBySellerId(sellerId).stream()
                .map(CommissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getCommissionsByStatus(CommissionStatus status) {
        return commissionRepository.findByStatus(status).stream()
                .map(CommissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getPendingBySeller(Long sellerId) {
        return commissionRepository.findBySellerIdAndStatus(sellerId, CommissionStatus.PENDING).stream()
                .map(CommissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Workflow ────────────────────────────────────────────────

    @Override
    @Transactional
    public CommissionResponse approve(Long id) {
        Commission commission = findEntityById(id);
        if (commission.getStatus() != CommissionStatus.PENDING) {
            throw new BusinessException("Only PENDING commissions can be approved. Current: " + commission.getStatus());
        }
        commission.setStatus(CommissionStatus.APPROVED);
        return CommissionMapper.toResponse(commissionRepository.save(commission));
    }

    @Override
    @Transactional
    public CommissionResponse pay(Long id) {
        Commission commission = findEntityById(id);
        if (commission.getStatus() != CommissionStatus.APPROVED) {
            throw new BusinessException("Only APPROVED commissions can be paid. Current: " + commission.getStatus());
        }
        commission.setStatus(CommissionStatus.PAID);
        commission.setPaymentDate(LocalDate.now());
        return CommissionMapper.toResponse(commissionRepository.save(commission));
    }

    @Override
    @Transactional
    public CommissionResponse cancel(Long id) {
        Commission commission = findEntityById(id);
        if (commission.getStatus() == CommissionStatus.PAID) {
            throw new BusinessException("Cannot cancel a paid commission");
        }
        commission.setStatus(CommissionStatus.CANCELLED);
        return CommissionMapper.toResponse(commissionRepository.save(commission));
    }

    // ── Relatórios ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidBySeller(Long sellerId) {
        return commissionRepository.calculateTotalPaidBySeller(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPendingBySeller(Long sellerId) {
        return commissionRepository.calculateTotalPendingBySeller(sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionResponse> getDueForPayment() {
        return commissionRepository.findDueForPayment(LocalDate.now()).stream()
                .map(CommissionMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Helpers ────────────────────────────────────────────────

    private Commission findEntityById(Long id) {
        return commissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commission", id));
    }
}
