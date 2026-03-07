package com.erp.moveis.sales.service;

import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteItem;
import com.erp.moveis.sales.entity.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuoteService {

    Quote createQuote(Quote quote);

    Quote findById(Long id);

    Quote findFullQuote(Long id);

    List<Quote> findByCompany(Long companyId);

    Page<Quote> findByCompany(Long companyId, Pageable pageable);

    List<Quote> findByStatus(QuoteStatus status);

    Quote updateStatus(Long id, QuoteStatus status);

    Quote approve(Long id);

    Quote reject(Long id);

    Quote addItem(Long quoteId, QuoteItem item);

    void removeItem(Long quoteId, Long itemId);

    Quote calculateTotals(Long id);

    void deleteQuote(Long id);
}
