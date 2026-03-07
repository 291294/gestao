package com.erp.moveis.invoicing.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.invoicing.dto.InvoiceItemRequest;
import com.erp.moveis.invoicing.dto.InvoiceRequest;
import com.erp.moveis.invoicing.dto.InvoiceResponse;
import com.erp.moveis.invoicing.entity.Invoice;
import com.erp.moveis.invoicing.entity.InvoiceItem;
import com.erp.moveis.invoicing.entity.InvoiceStatus;
import com.erp.moveis.invoicing.mapper.InvoiceMapper;
import com.erp.moveis.invoicing.repository.InvoiceRepository;
import com.erp.moveis.model.Order;
import com.erp.moveis.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;

    // ── CRUD ───────────────────────────────────────────────────

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Invoice invoice = InvoiceMapper.toEntity(request);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);

        if (invoice.getItems() != null) {
            invoice.getItems().forEach(InvoiceItem::calculateSubtotal);
        }
        recalculate(invoice);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(Long id) {
        Invoice invoice = invoiceRepository.findFullInvoice(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        return InvoiceMapper.toResponse(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getByCompany(Long companyId, Pageable pageable) {
        return invoiceRepository.findByCompanyId(companyId, pageable)
                .map(InvoiceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getByClient(Long clientId) {
        return invoiceRepository.findByClientId(clientId).stream()
                .map(InvoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Workflow ────────────────────────────────────────────────

    @Override
    @Transactional
    public InvoiceResponse issue(Long id) {
        Invoice invoice = findEntityById(id);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Only DRAFT invoices can be issued. Current: " + invoice.getStatus());
        }
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssueDate(LocalDate.now());
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceResponse send(Long id) {
        Invoice invoice = findEntityById(id);
        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new BusinessException("Only ISSUED invoices can be sent. Current: " + invoice.getStatus());
        }
        invoice.setStatus(InvoiceStatus.SENT);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceResponse cancel(Long id) {
        Invoice invoice = findEntityById(id);
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new BusinessException("Cannot cancel a fully paid invoice");
        }
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceResponse registerPayment(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Payment amount must be positive");
        }
        Invoice invoice = findEntityById(id);
        BigDecimal currentPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount);
        invoice.setAmountPaid(newPaid);

        if (invoice.isFullyPaid()) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    // ── Itens ──────────────────────────────────────────────────

    @Override
    @Transactional
    public InvoiceResponse addItem(Long invoiceId, InvoiceItemRequest itemRequest) {
        Invoice invoice = findEntityById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new BusinessException("Items can only be added to DRAFT invoices");
        }
        InvoiceItem item = InvoiceMapper.toItemEntity(itemRequest);
        invoice.addItem(item);
        item.calculateSubtotal();
        recalculate(invoice);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceResponse calculateTotals(Long id) {
        Invoice invoice = invoiceRepository.findFullInvoice(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
        invoice.getItems().forEach(InvoiceItem::calculateSubtotal);
        recalculate(invoice);
        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    // ── Relatórios ─────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponse> getOverdue() {
        return invoiceRepository.findOverdue(LocalDate.now()).stream()
                .map(InvoiceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getOpenBalanceByClient(Long clientId) {
        return invoiceRepository.calculateOpenBalanceByClient(clientId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue(Long companyId) {
        return invoiceRepository.calculateTotalRevenueByCompany(companyId);
    }

    // ── Pipeline: Order → Invoice ──────────────────────────────

    @Override
    @Transactional
    public InvoiceResponse createFromOrder(Long orderId, Long companyId, Long clientId) {
        invoiceRepository.findByOrderId(orderId).ifPresent(existing -> {
            throw new BusinessException("Invoice already exists for order " + orderId);
        });

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        Invoice invoice = new Invoice();
        invoice.setCompanyId(companyId);
        invoice.setClientId(clientId);
        invoice.setOrderId(orderId);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setSubtotal(BigDecimal.valueOf(order.getTotalValue() != null ? order.getTotalValue() : 0));
        invoice.setTotalAmount(invoice.getSubtotal());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        return InvoiceMapper.toResponse(invoiceRepository.save(invoice));
    }

    // ── Helpers ────────────────────────────────────────────────

    private Invoice findEntityById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", id));
    }

    private void recalculate(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                if (item.getSubtotal() != null) {
                    subtotal = subtotal.add(item.getSubtotal());
                }
            }
        }
        invoice.setSubtotal(subtotal);

        BigDecimal discount = invoice.getDiscountAmount() != null ? invoice.getDiscountAmount() : BigDecimal.ZERO;
        BigDecimal tax = invoice.getTaxAmount() != null ? invoice.getTaxAmount() : BigDecimal.ZERO;
        invoice.setTotalAmount(subtotal.subtract(discount).add(tax));
    }

    private String generateInvoiceNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "NF-" + year + "-" + uid;
    }
}
