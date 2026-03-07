package com.erp.moveis.sales.repository;

import com.erp.moveis.sales.model.Quote;
import com.erp.moveis.sales.model.Quote.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    Optional<Quote> findByQuoteNumber(String quoteNumber);

    List<Quote> findByCompanyId(Long companyId);

    List<Quote> findByClientId(Long clientId);

    List<Quote> findBySellerIdAndStatus(Long sellerId, QuoteStatus status);

    List<Quote> findByStatus(QuoteStatus status);

    @Query("SELECT q FROM Quote q WHERE q.validUntil < :date AND q.status IN ('SENT', 'APPROVED')")
    List<Quote> findExpiredQuotes(@Param("date") LocalDate date);

    @Query("SELECT q FROM Quote q WHERE q.seller.id = :sellerId AND q.createdAt >= :startDate AND q.createdAt <= :endDate")
    List<Quote> findBySellerAndPeriod(
            @Param("sellerId") Long sellerId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.company.id = :companyId AND q.status = :status")
    Long countByCompanyAndStatus(@Param("companyId") Long companyId, @Param("status") QuoteStatus status);

    @Query("SELECT COALESCE(SUM(q.finalAmount), 0) FROM Quote q WHERE q.seller.id = :sellerId AND q.status = 'CONVERTED'")
    java.math.BigDecimal calculateTotalConvertedByySeller(@Param("sellerId") Long sellerId);
}
