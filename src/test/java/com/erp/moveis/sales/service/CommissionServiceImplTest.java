package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.core.tenant.TenantContext;
import com.erp.moveis.sales.dto.CommissionResponse;
import com.erp.moveis.sales.entity.Commission;
import com.erp.moveis.sales.entity.Commission.CommissionStatus;
import com.erp.moveis.sales.mapper.CommissionMapper;
import com.erp.moveis.sales.repository.CommissionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommissionServiceImplTest {

    @Mock private CommissionRepository commissionRepository;
    @InjectMocks private CommissionServiceImpl service;

    private Commission commission;
    private CommissionResponse response;
    private MockedStatic<TenantContext> tenantContextMock;
    private static final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::requireTenantId).thenReturn(COMPANY_ID);
        commission = Commission.builder()
                .id(1L)
                .companyId(1L)
                .sellerId(10L)
                .orderId(100L)
                .commissionPercentage(new BigDecimal("10"))
                .saleAmount(new BigDecimal("5000"))
                .status(CommissionStatus.PENDING)
                .build();

        response = new CommissionResponse();
        response.setId(1L);
        response.setStatus(CommissionStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test @DisplayName("getCommission — should return commission by ID")
    void shouldGetCommission() {
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(commission)).thenReturn(response);
            CommissionResponse result = service.getCommission(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test @DisplayName("getCommission — should throw when not found")
    void shouldThrowWhenNotFound() {
        when(commissionRepository.findByIdAndCompanyId(999L, COMPANY_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getCommission(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("getCommissionsBySeller — should return seller commissions")
    void shouldGetBySeller() {
        when(commissionRepository.findByCompanyIdAndSellerId(COMPANY_ID, 10L)).thenReturn(List.of(commission));
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(commission)).thenReturn(response);
            List<CommissionResponse> result = service.getCommissionsBySeller(10L);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("getCommissionsByStatus — should return filtered commissions")
    void shouldGetByStatus() {
        when(commissionRepository.findByCompanyIdAndStatus(COMPANY_ID, CommissionStatus.PENDING)).thenReturn(List.of(commission));
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(commission)).thenReturn(response);
            List<CommissionResponse> result = service.getCommissionsByStatus(CommissionStatus.PENDING);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("getPendingBySeller — should return pending commissions")
    void shouldGetPendingBySeller() {
        when(commissionRepository.findByCompanyIdAndSellerIdAndStatus(COMPANY_ID, 10L, CommissionStatus.PENDING))
                .thenReturn(List.of(commission));
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(commission)).thenReturn(response);
            List<CommissionResponse> result = service.getPendingBySeller(10L);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("approve — should approve pending commission")
    void shouldApprove() {
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        when(commissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CommissionResponse approvedResp = new CommissionResponse();
        approvedResp.setStatus(CommissionStatus.APPROVED);
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(any(Commission.class))).thenReturn(approvedResp);
            service.approve(1L);
            assertThat(commission.getStatus()).isEqualTo(CommissionStatus.APPROVED);
        }
    }

    @Test @DisplayName("approve — should reject non-pending commission")
    void shouldRejectApproveNonPending() {
        commission.setStatus(CommissionStatus.PAID);
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        assertThatThrownBy(() -> service.approve(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    @Test @DisplayName("pay — should pay approved commission")
    void shouldPay() {
        commission.setStatus(CommissionStatus.APPROVED);
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        when(commissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(any(Commission.class))).thenReturn(response);
            service.pay(1L);
            assertThat(commission.getStatus()).isEqualTo(CommissionStatus.PAID);
            assertThat(commission.getPaymentDate()).isNotNull();
        }
    }

    @Test @DisplayName("pay — should reject non-approved commission")
    void shouldRejectPayNonApproved() {
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        assertThatThrownBy(() -> service.pay(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APPROVED");
    }

    @Test @DisplayName("cancel — should cancel non-paid commission")
    void shouldCancel() {
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        when(commissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(any(Commission.class))).thenReturn(response);
            service.cancel(1L);
            assertThat(commission.getStatus()).isEqualTo(CommissionStatus.CANCELLED);
        }
    }

    @Test @DisplayName("cancel — should reject cancelling paid commission")
    void shouldRejectCancelPaid() {
        commission.setStatus(CommissionStatus.PAID);
        when(commissionRepository.findByIdAndCompanyId(1L, COMPANY_ID)).thenReturn(Optional.of(commission));
        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("paid");
    }

    @Test @DisplayName("getTotalPaidBySeller — should return total")
    void shouldGetTotalPaid() {
        when(commissionRepository.calculateTotalPaidBySeller(COMPANY_ID, 10L)).thenReturn(new BigDecimal("15000"));
        BigDecimal total = service.getTotalPaidBySeller(10L);
        assertThat(total).isEqualByComparingTo(new BigDecimal("15000"));
    }

    @Test @DisplayName("getDueForPayment — should return due commissions")
    void shouldGetDueForPayment() {
        when(commissionRepository.findDueForPayment(anyLong(), any(LocalDate.class))).thenReturn(List.of(commission));
        try (MockedStatic<CommissionMapper> mapper = mockStatic(CommissionMapper.class)) {
            mapper.when(() -> CommissionMapper.toResponse(commission)).thenReturn(response);
            List<CommissionResponse> result = service.getDueForPayment();
            assertThat(result).hasSize(1);
        }
    }
}
