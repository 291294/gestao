package com.erp.moveis.sales.service;

import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteStatus;

import java.util.List;
import java.util.Optional;

public interface QuoteService {

    Quote createQuote(Quote quote);

    Optional<Quote> findById(Long id);

    List<Quote> findByCompany(Long companyId);

    List<Quote> findByStatus(QuoteStatus status);

    Quote updateStatus(Long id, QuoteStatus status);

    void deleteQuote(Long id);
}
