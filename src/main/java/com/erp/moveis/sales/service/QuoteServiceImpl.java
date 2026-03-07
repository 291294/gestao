package com.erp.moveis.sales.service;

import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;

    @Override
    public Quote createQuote(Quote quote) {
        quote.setStatus(QuoteStatus.DRAFT);
        return quoteRepository.save(quote);
    }

    @Override
    public Optional<Quote> findById(Long id) {
        return quoteRepository.findById(id);
    }

    @Override
    public List<Quote> findByCompany(Long companyId) {
        return quoteRepository.findByCompanyId(companyId);
    }

    @Override
    public List<Quote> findByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status);
    }

    @Override
    public Quote updateStatus(Long id, QuoteStatus status) {
        Quote quote = quoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quote not found"));
        quote.setStatus(status);
        return quoteRepository.save(quote);
    }

    @Override
    public void deleteQuote(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw new RuntimeException("Quote not found");
        }
        quoteRepository.deleteById(id);
    }
}
