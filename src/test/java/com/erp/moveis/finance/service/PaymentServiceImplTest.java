package com.erp.moveis.finance.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.finance.dto.PaymentRequest;
import com.erp.moveis.finance.dto.PaymentResponse;
import com.erp.moveis.finance.entity.Payment;
import com.erp.moveis.finance.entity.PaymentMethod;
import com.erp.moveis.finance.entity.PaymentStatus;
import com.erp.moveis.finance.repository.PaymentRepository;
import com.erp.moveis.invoicing.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private InvoiceService invoiceService;
    @InjectMocks private PaymentServiceImpl paymentService;

    private Payment samplePayment;

    @BeforeEach
    void setUp() {
        samplePayment = Payment.builder()
                .id(1L)
                .companyId(1L)
                .invoiceId(10L)
                .paymentNumber("PAG-2026-ABC12345")
                .amount(new BigDecimal("1500.00"))
                .paymentMethod(PaymentMethod.PIX)
                .status(PaymentStatus.PENDING)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    // ── CREATE ──────────────────────────────────────────────

    @Test
    @DisplayName("createPayment — should create a pending payment with generated number")
    void shouldCreatePayment() {
        PaymentRequest request = PaymentRequest.builder()
                .companyId(1L)
                .invoiceId(10L)
                .amount(new BigDecimal("1500.00"))
                .paymentMethod("PIX")
                .notes("Pagamento via PIX")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        assertThat(response.getPaymentNumber()).startsWith("PAG-");
        verify(paymentRepository).save(any(Payment.class));
    }

    // ── GET ────────────────────────────────────────────────

    @Test
    @DisplayName("getPayment — should return payment by ID")
    void shouldGetPaymentById() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        PaymentResponse response = paymentService.getPayment(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("getPayment — should throw ResourceNotFoundException for non-existent payment")
    void shouldThrowWhenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getByInvoice — should return payments for invoice")
    void shouldGetByInvoice() {
        when(paymentRepository.findByInvoiceId(10L)).thenReturn(List.of(samplePayment));

        List<PaymentResponse> result = paymentService.getByInvoice(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInvoiceId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getByCompany — should return payments for company")
    void shouldGetByCompany() {
        when(paymentRepository.findByCompanyId(1L)).thenReturn(List.of(samplePayment));

        List<PaymentResponse> result = paymentService.getByCompany(1L);

        assertThat(result).hasSize(1);
    }

    // ── CONFIRM ────────────────────────────────────────────

    @Test
    @DisplayName("confirm — should confirm a pending payment")
    void shouldConfirmPendingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.confirm(1L);

        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(invoiceService).registerPayment(eq(10L), eq(new BigDecimal("1500.00")));
    }

    @Test
    @DisplayName("confirm — should reject confirming a non-pending payment")
    void shouldRejectConfirmNonPending() {
        samplePayment.setStatus(PaymentStatus.CANCELLED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        assertThatThrownBy(() -> paymentService.confirm(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PENDING");
    }

    // ── CANCEL ─────────────────────────────────────────────

    @Test
    @DisplayName("cancel — should cancel a pending payment")
    void shouldCancelPendingPayment() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.cancel(1L);

        assertThat(response.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("cancel — should reject cancelling a confirmed payment")
    void shouldRejectCancelConfirmed() {
        samplePayment.setStatus(PaymentStatus.CONFIRMED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        assertThatThrownBy(() -> paymentService.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("confirmed");
    }

    // ── REFUND ─────────────────────────────────────────────

    @Test
    @DisplayName("refund — should refund a confirmed payment")
    void shouldRefundConfirmedPayment() {
        samplePayment.setStatus(PaymentStatus.CONFIRMED);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentResponse response = paymentService.refund(1L);

        assertThat(response.getStatus()).isEqualTo("REFUNDED");
    }

    @Test
    @DisplayName("refund — should reject refunding a non-confirmed payment")
    void shouldRejectRefundNonConfirmed() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(samplePayment));

        assertThatThrownBy(() -> paymentService.refund(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CONFIRMED");
    }

    // ── AGGREGATIONS ───────────────────────────────────────

    @Test
    @DisplayName("getTotalConfirmedByInvoice — should return total")
    void shouldGetTotalByInvoice() {
        when(paymentRepository.sumConfirmedByInvoice(10L)).thenReturn(new BigDecimal("3000.00"));

        BigDecimal total = paymentService.getTotalConfirmedByInvoice(10L);

        assertThat(total).isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("getRevenueByPeriod — should return revenue sum")
    void shouldGetRevenue() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(paymentRepository.sumConfirmedByCompanyAndPeriod(1L, start, end))
                .thenReturn(new BigDecimal("50000.00"));

        BigDecimal revenue = paymentService.getRevenueByPeriod(1L, start, end);

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("50000.00"));
    }

    @Test
    @DisplayName("getByCompanyAndPeriod — should return payments in period")
    void shouldGetByPeriod() {
        LocalDateTime start = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(paymentRepository.findByCompanyIdAndPeriod(1L, start, end))
                .thenReturn(List.of(samplePayment));

        List<PaymentResponse> result = paymentService.getByCompanyAndPeriod(1L, start, end);

        assertThat(result).hasSize(1);
    }
}
