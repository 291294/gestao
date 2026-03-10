package com.erp.moveis.invoicing.repository;

import com.erp.moveis.invoicing.entity.Invoice;
import com.erp.moveis.invoicing.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCompanyId(Long companyId);

    Page<Invoice> findByCompanyId(Long companyId, Pageable pageable);

    List<Invoice> findByClientId(Long clientId);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByCompanyIdAndStatus(Long companyId, InvoiceStatus status);

    Optional<Invoice> findByOrderId(Long orderId);

    @Query("SELECT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Optional<Invoice> findFullInvoice(@Param("id") Long id);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :date AND i.status IN ('ISSUED', 'SENT', 'PARTIALLY_PAID')")
    List<Invoice> findOverdue(@Param("date") LocalDate date);

    @Query("SELECT i FROM Invoice i WHERE i.dueDate < CURRENT_DATE AND i.status IN ('ISSUED', 'SENT', 'PARTIALLY_PAID')")
    List<Invoice> findOverdueInvoices();

    @Query("SELECT COALESCE(SUM(i.totalAmount - i.amountPaid), 0) FROM Invoice i WHERE i.clientId = :clientId AND i.status IN ('ISSUED', 'SENT', 'PARTIALLY_PAID', 'OVERDUE')")
    BigDecimal calculateOpenBalanceByClient(@Param("clientId") Long clientId);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.companyId = :companyId AND i.status = 'PAID'")
    BigDecimal calculateTotalRevenueByCompany(@Param("companyId") Long companyId);
}
