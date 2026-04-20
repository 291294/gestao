package com.erp.moveis.invoicing.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.invoicing.dto.InvoiceResponse;
import com.erp.moveis.invoicing.entity.Invoice;
import com.erp.moveis.invoicing.entity.InvoiceStatus;
import com.erp.moveis.invoicing.mapper.InvoiceMapper;
import com.erp.moveis.invoicing.repository.InvoiceRepository;
import com.erp.moveis.model.Order;
import com.erp.moveis.repository.OrderRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceImplTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private InvoiceServiceImpl service;

    private Invoice invoice;
    private InvoiceResponse response;

    @BeforeEach
    void setUp() {
        invoice = new Invoice();
        invoice.setId(1L);
        invoice.setCompanyId(1L);
        invoice.setClientId(5L);
        invoice.setInvoiceNumber("NF-2026-ABC12345");
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setSubtotal(new BigDecimal("1000"));
        invoice.setTotalAmount(new BigDecimal("1000"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setDiscountAmount(BigDecimal.ZERO);
        invoice.setTaxAmount(BigDecimal.ZERO);
        invoice.setItems(new ArrayList<>());

        response = new InvoiceResponse();
        response.setId(1L);
        response.setStatus(InvoiceStatus.DRAFT);
    }

    @Test @DisplayName("getInvoice — should return invoice by ID")
    void shouldGetInvoice() {
        when(invoiceRepository.findFullInvoice(1L)).thenReturn(Optional.of(invoice));
        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(invoice)).thenReturn(response);
            InvoiceResponse result = service.getInvoice(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test @DisplayName("getInvoice — should throw when not found")
    void shouldThrowWhenInvoiceNotFound() {
        when(invoiceRepository.findFullInvoice(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getInvoice(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("issue — should set status ISSUED for DRAFT invoice")
    void shouldIssue() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceResponse issuedResp = new InvoiceResponse();
        issuedResp.setStatus(InvoiceStatus.ISSUED);
        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(any(Invoice.class))).thenReturn(issuedResp);
            InvoiceResponse result = service.issue(1L);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
        }
    }

    @Test @DisplayName("issue — should reject non-DRAFT invoices")
    void shouldRejectIssueNonDraft() {
        invoice.setStatus(InvoiceStatus.SENT);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThatThrownBy(() -> service.issue(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test @DisplayName("send — should set status SENT for ISSUED invoice")
    void shouldSend() {
        invoice.setStatus(InvoiceStatus.ISSUED);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(any(Invoice.class))).thenReturn(response);
            service.send(1L);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.SENT);
        }
    }

    @Test @DisplayName("send — should reject non-ISSUED invoices")
    void shouldRejectSendNonIssued() {
        invoice.setStatus(InvoiceStatus.DRAFT);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThatThrownBy(() -> service.send(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ISSUED");
    }

    @Test @DisplayName("cancel — should cancel non-paid invoice")
    void shouldCancel() {
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(any(Invoice.class))).thenReturn(response);
            service.cancel(1L);
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.CANCELLED);
        }
    }

    @Test @DisplayName("cancel — should reject cancelling paid invoice")
    void shouldRejectCancelPaid() {
        invoice.setStatus(InvoiceStatus.PAID);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        assertThatThrownBy(() -> service.cancel(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("paid");
    }

    @Test @DisplayName("registerPayment — should accept partial payment")
    void shouldRegisterPartialPayment() {
        invoice.setTotalAmount(new BigDecimal("1000"));
        invoice.setAmountPaid(BigDecimal.ZERO);
        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(any(Invoice.class))).thenReturn(response);
            service.registerPayment(1L, new BigDecimal("500"));
            assertThat(invoice.getAmountPaid()).isEqualByComparingTo(new BigDecimal("500"));
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PARTIALLY_PAID);
        }
    }

    @Test @DisplayName("registerPayment — should reject negative amount")
    void shouldRejectNegativePayment() {
        assertThatThrownBy(() -> service.registerPayment(1L, new BigDecimal("-10")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("positive");
    }

    @Test @DisplayName("getOverdue — should return overdue invoices")
    void shouldGetOverdue() {
        when(invoiceRepository.findOverdue(any(LocalDate.class))).thenReturn(List.of(invoice));
        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            mapper.when(() -> InvoiceMapper.toResponse(invoice)).thenReturn(response);
            List<InvoiceResponse> result = service.getOverdue();
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("createFromOrder — should create invoice from order")
    void shouldCreateFromOrder() {
        Order order = new Order();
        order.setId(10L);
        order.setTotalValue(new BigDecimal("5000"));
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(2L);
            return i;
        });

        try (MockedStatic<InvoiceMapper> mapper = mockStatic(InvoiceMapper.class)) {
            InvoiceResponse newResp = new InvoiceResponse();
            newResp.setId(2L);
            mapper.when(() -> InvoiceMapper.toResponse(any(Invoice.class))).thenReturn(newResp);
            InvoiceResponse result = service.createFromOrder(10L, 1L, 5L);
            assertThat(result.getId()).isEqualTo(2L);
        }
    }

    @Test @DisplayName("createFromOrder — should reject duplicate")
    void shouldRejectDuplicateOrderInvoice() {
        when(invoiceRepository.findByOrderId(10L)).thenReturn(Optional.of(invoice));
        assertThatThrownBy(() -> service.createFromOrder(10L, 1L, 5L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }
}
