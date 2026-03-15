package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.sales.dto.SalesTargetResponse;
import com.erp.moveis.sales.entity.SalesTarget;
import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;
import com.erp.moveis.sales.mapper.SalesTargetMapper;
import com.erp.moveis.sales.repository.SalesTargetRepository;
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
class SalesTargetServiceImplTest {

    @Mock private SalesTargetRepository salesTargetRepository;
    @InjectMocks private SalesTargetServiceImpl service;

    private SalesTarget target;
    private SalesTargetResponse response;

    @BeforeEach
    void setUp() {
        target = SalesTarget.builder()
                .id(1L)
                .companyId(1L)
                .sellerId(10L)
                .targetAmount(new BigDecimal("50000"))
                .achievedAmount(BigDecimal.ZERO)
                .achievementPercentage(BigDecimal.ZERO)
                .status(TargetStatus.ACTIVE)
                .targetType(TargetType.INDIVIDUAL)
                .periodStart(LocalDate.of(2026, 1, 1))
                .periodEnd(LocalDate.of(2026, 12, 31))
                .build();

        response = new SalesTargetResponse();
        response.setId(1L);
        response.setStatus(TargetStatus.ACTIVE);
    }

    @Test @DisplayName("getTarget — should return target by ID")
    void shouldGetTarget() {
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(target)).thenReturn(response);
            SalesTargetResponse result = service.getTarget(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test @DisplayName("getTarget — should throw when not found")
    void shouldThrowWhenNotFound() {
        when(salesTargetRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getTarget(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("getTargetsBySeller — should return filtered targets")
    void shouldGetBySeller() {
        when(salesTargetRepository.findBySellerIdAndStatus(10L, TargetStatus.ACTIVE))
                .thenReturn(List.of(target));
        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(target)).thenReturn(response);
            List<SalesTargetResponse> result = service.getTargetsBySeller(10L, TargetStatus.ACTIVE);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("getTargetsByCompany — should return company targets by type")
    void shouldGetByCompanyAndType() {
        when(salesTargetRepository.findByCompanyIdAndTargetType(1L, TargetType.INDIVIDUAL))
                .thenReturn(List.of(target));
        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(target)).thenReturn(response);
            List<SalesTargetResponse> result = service.getTargetsByCompany(1L, TargetType.INDIVIDUAL);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("addAchievedAmount — should update amount and percentage")
    void shouldAddAchievedAmount() {
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        when(salesTargetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(any(SalesTarget.class))).thenReturn(response);
            service.addAchievedAmount(1L, new BigDecimal("10000"));
            assertThat(target.getAchievedAmount()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(target.getStatus()).isEqualTo(TargetStatus.ACTIVE);
        }
    }

    @Test @DisplayName("addAchievedAmount — should auto-complete at 100%")
    void shouldAutoCompleteAt100() {
        target.setTargetAmount(new BigDecimal("1000"));
        target.setAchievedAmount(BigDecimal.ZERO);
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        when(salesTargetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(any(SalesTarget.class))).thenReturn(response);
            service.addAchievedAmount(1L, new BigDecimal("1000"));
            assertThat(target.getStatus()).isEqualTo(TargetStatus.COMPLETED);
        }
    }

    @Test @DisplayName("addAchievedAmount — should reject non-active target")
    void shouldRejectAddToNonActive() {
        target.setStatus(TargetStatus.COMPLETED);
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        assertThatThrownBy(() -> service.addAchievedAmount(1L, new BigDecimal("100")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ACTIVE");
    }

    @Test @DisplayName("complete — should complete active target")
    void shouldComplete() {
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        when(salesTargetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(any(SalesTarget.class))).thenReturn(response);
            service.complete(1L);
            assertThat(target.getStatus()).isEqualTo(TargetStatus.COMPLETED);
        }
    }

    @Test @DisplayName("cancel — should cancel non-completed target")
    void shouldCancel() {
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        when(salesTargetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SalesTargetMapper> mapper = mockStatic(SalesTargetMapper.class)) {
            mapper.when(() -> SalesTargetMapper.toResponse(any(SalesTarget.class))).thenReturn(response);
            service.cancel(1L);
            assertThat(target.getStatus()).isEqualTo(TargetStatus.CANCELLED);
        }
    }

    @Test @DisplayName("cancel — should reject cancelling completed target")
    void shouldRejectCancelCompleted() {
        target.setStatus(TargetStatus.COMPLETED);
        when(salesTargetRepository.findById(1L)).thenReturn(Optional.of(target));
        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("completed");
    }

    @Test @DisplayName("closeExpiredTargets — should close all expired targets")
    void shouldCloseExpired() {
        SalesTarget expired = SalesTarget.builder()
                .id(2L).status(TargetStatus.ACTIVE)
                .periodEnd(LocalDate.now().minusDays(1))
                .build();
        when(salesTargetRepository.findExpiredTargets(any(LocalDate.class)))
                .thenReturn(List.of(expired));
        when(salesTargetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.closeExpiredTargets();
        assertThat(expired.getStatus()).isEqualTo(TargetStatus.COMPLETED);
        verify(salesTargetRepository).save(expired);
    }
}
