package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteItem;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.repository.QuoteItemRepository;
import com.erp.moveis.sales.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;

    @Override
    @Transactional
    public Quote createQuote(Quote quote) {
        quote.setQuoteNumber(generateQuoteNumber());
        quote.setStatus(QuoteStatus.DRAFT);
        recalculate(quote);
        return quoteRepository.save(quote);
    }

    @Override
    public Quote findById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
    }

    @Override
    public Quote findFullQuote(Long id) {
        return quoteRepository.findFullQuote(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
    }

    @Override
    public List<Quote> findByCompany(Long companyId) {
        return quoteRepository.findByCompanyId(companyId);
    }

    @Override
    public Page<Quote> findByCompany(Long companyId, Pageable pageable) {
        return quoteRepository.findByCompanyId(companyId, pageable);
    }

    @Override
    public List<Quote> findByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public Quote updateStatus(Long id, QuoteStatus status) {
        Quote quote = findById(id);
        quote.setStatus(status);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote approve(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus() != QuoteStatus.SENT && quote.getStatus() != QuoteStatus.DRAFT) {
            throw new BusinessException("Only DRAFT or SENT quotes can be approved. Current: " + quote.getStatus());
        }
        quote.setStatus(QuoteStatus.APPROVED);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote reject(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new BusinessException("Cannot reject a converted quote");
        }
        quote.setStatus(QuoteStatus.REJECTED);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote addItem(Long quoteId, QuoteItem item) {
        Quote quote = findById(quoteId);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new BusinessException("Items can only be added to DRAFT quotes");
        }
        quote.addItem(item);
        item.calculateSubtotal();
        recalculate(quote);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public void removeItem(Long quoteId, Long itemId) {
        Quote quote = findById(quoteId);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new BusinessException("Items can only be removed from DRAFT quotes");
        }
        QuoteItem item = quoteItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("QuoteItem", itemId));
        quote.removeItem(item);
        quoteItemRepository.delete(item);
        recalculate(quote);
        quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public Quote calculateTotals(Long id) {
        Quote quote = findFullQuote(id);
        recalculate(quote);
        return quoteRepository.save(quote);
    }

    @Override
    @Transactional
    public void deleteQuote(Long id) {
        Quote quote = findById(id);
        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new BusinessException("Cannot delete a converted quote");
        }
        quoteRepository.delete(quote);
    }

    // ── Helpers ────────────────────────────────────────────────

    private void recalculate(Quote quote) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (quote.getItems() != null) {
            for (QuoteItem item : quote.getItems()) {
                if (item.getSubtotal() != null) {
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
            }
        }
        quote.setTotalAmount(totalAmount);

        BigDecimal discount = quote.getDiscountAmount() != null ? quote.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = quote.getTaxAmount() != null ? quote.getTaxAmount() : BigDecimal.ZERO;
        quote.setFinalAmount(totalAmount.subtract(discount).add(tax));
    }

    private String generateQuoteNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORC-" + year + "-" + uid;
    }
}
