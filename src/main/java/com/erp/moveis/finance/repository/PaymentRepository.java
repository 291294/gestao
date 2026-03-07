package com.erp.moveis.finance.repository;

import com.erp.moveis.finance.entity.Payment;
import com.erp.moveis.finance.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByInvoiceId(Long invoiceId);

    List<Payment> findByCompanyId(Long companyId);

    List<Payment> findByCompanyIdAndStatus(Long companyId, PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.companyId = :companyId AND p.paymentDate BETWEEN :start AND :end")
    List<Payment> findByCompanyIdAndPeriod(@Param("companyId") Long companyId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.invoiceId = :invoiceId AND p.status = 'CONFIRMED'")
    BigDecimal sumConfirmedByInvoice(@Param("invoiceId") Long invoiceId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.companyId = :companyId AND p.status = 'CONFIRMED' AND p.paymentDate BETWEEN :start AND :end")
    BigDecimal sumConfirmedByCompanyAndPeriod(@Param("companyId") Long companyId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);
}
