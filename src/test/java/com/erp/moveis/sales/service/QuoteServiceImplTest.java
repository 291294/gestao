package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.model.Client;
import com.erp.moveis.model.Order;
import com.erp.moveis.repository.ClientRepository;
import com.erp.moveis.repository.OrderRepository;
import com.erp.moveis.sales.dto.QuoteResponse;
import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteItem;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.mapper.QuoteMapper;
import com.erp.moveis.sales.repository.CommissionRepository;
import com.erp.moveis.sales.repository.QuoteItemRepository;
import com.erp.moveis.sales.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceImplTest {

    @Mock private QuoteRepository quoteRepository;
    @Mock private QuoteItemRepository quoteItemRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private CommissionRepository commissionRepository;
    @Mock private SalesTargetService salesTargetService;
    @InjectMocks private QuoteServiceImpl service;

    private Quote quote;
    private QuoteResponse response;

    @BeforeEach
    void setUp() {
        quote = Quote.builder()
                .id(1L)
                .companyId(1L)
                .clientId(5L)
                .sellerId(10L)
                .quoteNumber("ORC-2026-ABC12345")
                .status(QuoteStatus.DRAFT)
                .totalAmount(new BigDecimal("5000"))
                .discountAmount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .finalAmount(new BigDecimal("5000"))
                .items(new ArrayList<>())
                .build();

        response = new QuoteResponse();
        response.setId(1L);
        response.setStatus(QuoteStatus.DRAFT);
    }

    @Test @DisplayName("getQuote — should return quote by ID")
    void shouldGetQuote() {
        when(quoteRepository.findFullQuote(1L)).thenReturn(Optional.of(quote));
        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            mapper.when(() -> QuoteMapper.toResponse(quote)).thenReturn(response);
            QuoteResponse result = service.getQuote(1L);
            assertThat(result.getId()).isEqualTo(1L);
        }
    }

    @Test @DisplayName("getQuote — should throw when not found")
    void shouldThrowWhenNotFound() {
        when(quoteRepository.findFullQuote(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getQuote(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("getQuotesByCompany — should return company quotes")
    void shouldGetByCompany() {
        when(quoteRepository.findByCompanyId(1L)).thenReturn(List.of(quote));
        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            mapper.when(() -> QuoteMapper.toResponse(quote)).thenReturn(response);
            List<QuoteResponse> result = service.getQuotesByCompany(1L);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("getQuotesByStatus — should return filtered quotes")
    void shouldGetByStatus() {
        when(quoteRepository.findByStatus(QuoteStatus.DRAFT)).thenReturn(List.of(quote));
        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            mapper.when(() -> QuoteMapper.toResponse(quote)).thenReturn(response);
            List<QuoteResponse> result = service.getQuotesByStatus(QuoteStatus.DRAFT);
            assertThat(result).hasSize(1);
        }
    }

    @Test @DisplayName("approve — should approve DRAFT or SENT quote")
    void shouldApprove() {
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            mapper.when(() -> QuoteMapper.toResponse(any(Quote.class))).thenReturn(response);
            service.approve(1L);
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.APPROVED);
        }
    }

    @Test @DisplayName("approve — should reject already converted quote")
    void shouldRejectApproveConverted() {
        quote.setStatus(QuoteStatus.CONVERTED);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        assertThatThrownBy(() -> service.approve(1L))
                .isInstanceOf(BusinessException.class);
    }

    @Test @DisplayName("reject — should reject non-converted quote")
    void shouldReject() {
        quote.setStatus(QuoteStatus.SENT);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        when(quoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            mapper.when(() -> QuoteMapper.toResponse(any(Quote.class))).thenReturn(response);
            service.reject(1L);
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.REJECTED);
        }
    }

    @Test @DisplayName("reject — should throw for converted quote")
    void shouldThrowRejectConverted() {
        quote.setStatus(QuoteStatus.CONVERTED);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        assertThatThrownBy(() -> service.reject(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("converted");
    }

    @Test @DisplayName("addItem — should reject non-DRAFT quote")
    void shouldRejectAddItemNonDraft() {
        quote.setStatus(QuoteStatus.SENT);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        assertThatThrownBy(() -> service.addItem(1L, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("DRAFT");
    }

    @Test @DisplayName("deleteQuote — should delete non-converted quote")
    void shouldDelete() {
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        service.deleteQuote(1L);
        verify(quoteRepository).delete(quote);
    }

    @Test @DisplayName("deleteQuote — should reject deleting converted quote")
    void shouldRejectDeleteConverted() {
        quote.setStatus(QuoteStatus.CONVERTED);
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));
        assertThatThrownBy(() -> service.deleteQuote(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("converted");
    }

    @Test @DisplayName("convertToOrder — should create order from approved quote")
    void shouldConvertToOrder() {
        quote.setStatus(QuoteStatus.APPROVED);
        quote.setFinalAmount(new BigDecimal("5000"));

        Client client = new Client();
        client.setId(5L);

        Order savedOrder = new Order();
        savedOrder.setId(100L);

        when(quoteRepository.findFullQuote(1L)).thenReturn(Optional.of(quote));
        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(quoteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(commissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<QuoteMapper> mapper = mockStatic(QuoteMapper.class)) {
            QuoteResponse convertedResp = new QuoteResponse();
            convertedResp.setStatus(QuoteStatus.CONVERTED);
            mapper.when(() -> QuoteMapper.toResponse(any(Quote.class))).thenReturn(convertedResp);

            QuoteResponse result = service.convertToOrder(1L, new BigDecimal("10"));
            assertThat(quote.getStatus()).isEqualTo(QuoteStatus.CONVERTED);
            verify(orderRepository).save(any(Order.class));
            verify(commissionRepository).save(any());
            verify(salesTargetService).updateSellerTargets(eq(10L), any(BigDecimal.class));
        }
    }

    @Test @DisplayName("convertToOrder — should reject non-approved quote")
    void shouldRejectConvertNonApproved() {
        when(quoteRepository.findFullQuote(1L)).thenReturn(Optional.of(quote));
        assertThatThrownBy(() -> service.convertToOrder(1L, new BigDecimal("10")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("APPROVED");
    }
}
