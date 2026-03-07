package com.erp.moveis.sales.service;

import com.erp.moveis.sales.dto.QuoteItemRequest;
import com.erp.moveis.sales.dto.QuoteRequest;
import com.erp.moveis.sales.dto.QuoteResponse;
import com.erp.moveis.sales.entity.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuoteService {

    QuoteResponse createQuote(QuoteRequest request);

    QuoteResponse getQuote(Long id);

    List<QuoteResponse> getQuotesByCompany(Long companyId);

    Page<QuoteResponse> getQuotesByCompany(Long companyId, Pageable pageable);

    List<QuoteResponse> getQuotesByStatus(QuoteStatus status);

    QuoteResponse updateStatus(Long id, QuoteStatus status);

    QuoteResponse approve(Long id);

    QuoteResponse reject(Long id);

    QuoteResponse addItem(Long quoteId, QuoteItemRequest itemRequest);

    void removeItem(Long quoteId, Long itemId);

    QuoteResponse calculateTotals(Long id);

    void deleteQuote(Long id);
}
