package com.erp.moveis.sales.service;

import com.erp.moveis.sales.dto.CommissionRequest;
import com.erp.moveis.sales.dto.CommissionResponse;
import com.erp.moveis.sales.entity.Commission.CommissionStatus;

import java.math.BigDecimal;
import java.util.List;

public interface CommissionService {

    CommissionResponse createCommission(CommissionRequest request);

    CommissionResponse getCommission(Long id);

    List<CommissionResponse> getCommissionsBySeller(Long sellerId);

    List<CommissionResponse> getCommissionsByStatus(CommissionStatus status);

    List<CommissionResponse> getPendingBySeller(Long sellerId);

    CommissionResponse approve(Long id);

    CommissionResponse pay(Long id);

    CommissionResponse cancel(Long id);

    BigDecimal getTotalPaidBySeller(Long sellerId);

    BigDecimal getTotalPendingBySeller(Long sellerId);

    List<CommissionResponse> getDueForPayment();
}
