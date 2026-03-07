package com.erp.moveis.finance.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.finance.dto.PaymentRequest;
import com.erp.moveis.finance.dto.PaymentResponse;
import com.erp.moveis.finance.entity.Payment;
import com.erp.moveis.finance.entity.PaymentStatus;
import com.erp.moveis.finance.mapper.PaymentMapper;
import com.erp.moveis.finance.repository.PaymentRepository;
import com.erp.moveis.invoicing.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = PaymentMapper.toEntity(request);
        payment.setPaymentNumber(generatePaymentNumber());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentDate(LocalDateTime.now());
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        return PaymentMapper.toResponse(findEntityById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream()
                .map(PaymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByCompany(Long companyId) {
        return paymentRepository.findByCompanyId(companyId).stream()
                .map(PaymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentResponse confirm(Long id) {
        Payment payment = findEntityById(id);
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("Only PENDING payments can be confirmed. Current: " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.CONFIRMED);
        payment.setConfirmationDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // Registra o pagamento na nota fiscal
        invoiceService.registerPayment(payment.getInvoiceId(), payment.getAmount());

        return PaymentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PaymentResponse cancel(Long id) {
        Payment payment = findEntityById(id);
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new BusinessException("Cannot cancel a confirmed payment. Use refund instead.");
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentResponse refund(Long id) {
        Payment payment = findEntityById(id);
        if (payment.getStatus() != PaymentStatus.CONFIRMED) {
            throw new BusinessException("Only CONFIRMED payments can be refunded. Current: " + payment.getStatus());
        }
        payment.setStatus(PaymentStatus.REFUNDED);
        return PaymentMapper.toResponse(paymentRepository.save(payment));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalConfirmedByInvoice(Long invoiceId) {
        return paymentRepository.sumConfirmedByInvoice(invoiceId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByPeriod(Long companyId, LocalDateTime start, LocalDateTime end) {
        return paymentRepository.sumConfirmedByCompanyAndPeriod(companyId, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getByCompanyAndPeriod(Long companyId, LocalDateTime start, LocalDateTime end) {
        return paymentRepository.findByCompanyIdAndPeriod(companyId, start, end).stream()
                .map(PaymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Payment findEntityById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    }

    private String generatePaymentNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        String uid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "PAG-" + year + "-" + uid;
    }
}
