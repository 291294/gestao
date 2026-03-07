package com.erp.moveis.sales.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.model.Client;
import com.erp.moveis.model.Order;
import com.erp.moveis.repository.ClientRepository;
import com.erp.moveis.repository.OrderRepository;
import com.erp.moveis.sales.dto.QuoteItemRequest;
import com.erp.moveis.sales.dto.QuoteRequest;
import com.erp.moveis.sales.dto.QuoteResponse;
import com.erp.moveis.sales.entity.Commission;
import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteItem;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.mapper.QuoteMapper;
import com.erp.moveis.sales.repository.CommissionRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuoteServiceImpl implements QuoteService {

    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final CommissionRepository commissionRepository;
    private final SalesTargetService salesTargetService;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public QuoteResponse createQuote(QuoteRequest request) {
        Quote quote = QuoteMapper.toEntity(request);
        quote.setQuoteNumber(generateQuoteNumber());
        quote.setStatus(QuoteStatus.DRAFT);

        // calcula subtotal de cada item
        if (quote.getItems() != null) {
            quote.getItems().forEach(QuoteItem::calculateSubtotal);
        }

        recalculate(quote);
        Quote saved = quoteRepository.save(quote);
        return QuoteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteResponse getQuote(Long id) {
        Quote quote = quoteRepository.findFullQuote(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
        return QuoteMapper.toResponse(quote);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponse> getQuotesByCompany(Long companyId) {
        return quoteRepository.findByCompanyId(companyId).stream()
                .map(QuoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByCompany(Long companyId, Pageable pageable) {
        return quoteRepository.findByCompanyId(companyId, pageable)
                .map(QuoteMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuoteResponse> getQuotesByStatus(QuoteStatus status) {
        return quoteRepository.findByStatus(status).stream()
                .map(QuoteMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Workflow ────────────────────────────────────────────────

    @Override
    @Transactional
    public QuoteResponse updateStatus(Long id, QuoteStatus status) {
        Quote quote = findEntityById(id);
        quote.setStatus(status);
        return QuoteMapper.toResponse(quoteRepository.save(quote));
    }

    @Override
    @Transactional
    public QuoteResponse approve(Long id) {
        Quote quote = findEntityById(id);
        if (quote.getStatus() != QuoteStatus.SENT && quote.getStatus() != QuoteStatus.DRAFT) {
            throw new BusinessException("Only DRAFT or SENT quotes can be approved. Current: " + quote.getStatus());
        }
        quote.setStatus(QuoteStatus.APPROVED);
        return QuoteMapper.toResponse(quoteRepository.save(quote));
    }

    @Override
    @Transactional
    public QuoteResponse reject(Long id) {
        Quote quote = findEntityById(id);
        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new BusinessException("Cannot reject a converted quote");
        }
        quote.setStatus(QuoteStatus.REJECTED);
        return QuoteMapper.toResponse(quoteRepository.save(quote));
    }

    // ── Itens ──────────────────────────────────────────────────

    @Override
    @Transactional
    public QuoteResponse addItem(Long quoteId, QuoteItemRequest itemRequest) {
        Quote quote = findEntityById(quoteId);
        if (quote.getStatus() != QuoteStatus.DRAFT) {
            throw new BusinessException("Items can only be added to DRAFT quotes");
        }
        QuoteItem item = QuoteMapper.toItemEntity(itemRequest);
        quote.addItem(item);
        item.calculateSubtotal();
        recalculate(quote);
        return QuoteMapper.toResponse(quoteRepository.save(quote));
    }

    @Override
    @Transactional
    public void removeItem(Long quoteId, Long itemId) {
        Quote quote = findEntityById(quoteId);
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

    // ── Cálculos ───────────────────────────────────────────────

    @Override
    @Transactional
    public QuoteResponse calculateTotals(Long id) {
        Quote quote = quoteRepository.findFullQuote(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
        recalculate(quote);
        return QuoteMapper.toResponse(quoteRepository.save(quote));
    }

    @Override
    @Transactional
    public void deleteQuote(Long id) {
        Quote quote = findEntityById(id);
        if (quote.getStatus() == QuoteStatus.CONVERTED) {
            throw new BusinessException("Cannot delete a converted quote");
        }
        quoteRepository.delete(quote);
    }

    // ── Pipeline: Orçamento → Pedido → Comissão → Meta ───────

    @Override
    @Transactional
    public QuoteResponse convertToOrder(Long quoteId, BigDecimal commissionPercentage) {
        Quote quote = quoteRepository.findFullQuote(quoteId)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", quoteId));

        // 1. Validação: só APPROVED pode ser convertido
        if (quote.getStatus() != QuoteStatus.APPROVED) {
            throw new BusinessException("Only APPROVED quotes can be converted to orders. Current: " + quote.getStatus());
        }

        // 2. Criar Pedido (Order) a partir do orçamento
        Client client = clientRepository.findById(quote.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", quote.getClientId()));

        Order order = new Order();
        order.setClient(client);
        order.setTotalValue(quote.getFinalAmount().doubleValue());
        order.setStatus("CONFIRMED");
        Order savedOrder = orderRepository.save(order);

        // 3. Marcar orçamento como CONVERTED
        quote.setStatus(QuoteStatus.CONVERTED);
        Quote savedQuote = quoteRepository.save(quote);

        // 4. Criar Comissão automaticamente
        if (commissionPercentage != null && commissionPercentage.compareTo(BigDecimal.ZERO) > 0) {
            Commission commission = Commission.builder()
                    .companyId(quote.getCompanyId())
                    .sellerId(quote.getSellerId())
                    .orderId(savedOrder.getId())
                    .quoteId(quote.getId())
                    .commissionPercentage(commissionPercentage)
                    .saleAmount(quote.getFinalAmount())
                    .build();
            commission.calculateCommission();
            commissionRepository.save(commission);
        }

        // 5. Atualizar metas de vendas do vendedor
        salesTargetService.updateSellerTargets(quote.getSellerId(), quote.getFinalAmount());

        return QuoteMapper.toResponse(savedQuote);
    }

    // ── Helpers (private) ──────────────────────────────────────

    private Quote findEntityById(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quote", id));
    }

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
