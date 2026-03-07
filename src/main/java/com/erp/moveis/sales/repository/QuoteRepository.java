package com.erp.moveis.sales.repository;

import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {

    // ── Busca simples ──────────────────────────────────────────

    Optional<Quote> findByQuoteNumber(String quoteNumber);

    List<Quote> findByCompanyId(Long companyId);

    List<Quote> findByClientId(Long clientId);

    List<Quote> findByStatus(QuoteStatus status);

    List<Quote> findByCompanyIdAndStatus(Long companyId, QuoteStatus status);

    List<Quote> findBySellerIdAndStatus(Long sellerId, QuoteStatus status);

    // ── Paginação ──────────────────────────────────────────────

    Page<Quote> findByCompanyId(Long companyId, Pageable pageable);

    Page<Quote> findByCompanyIdAndStatus(Long companyId, QuoteStatus status, Pageable pageable);

    // ── JOIN FETCH (evita N+1) ─────────────────────────────────

    @Query("SELECT q FROM Quote q LEFT JOIN FETCH q.items WHERE q.id = :id")
    Optional<Quote> findFullQuote(@Param("id") Long id);

    // ── Queries de negócio ─────────────────────────────────────

    @Query("SELECT q FROM Quote q WHERE q.validUntil < :date AND q.status IN ('SENT', 'APPROVED')")
    List<Quote> findExpiredQuotes(@Param("date") LocalDate date);

    @Query("SELECT q FROM Quote q WHERE q.sellerId = :sellerId AND q.createdAt >= :startDate AND q.createdAt <= :endDate")
    List<Quote> findBySellerAndPeriod(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(q) FROM Quote q WHERE q.companyId = :companyId AND q.status = :status")
    Long countByCompanyAndStatus(@Param("companyId") Long companyId, @Param("status") QuoteStatus status);

    @Query("SELECT COALESCE(SUM(q.finalAmount), 0) FROM Quote q WHERE q.sellerId = :sellerId AND q.status = 'CONVERTED'")
    BigDecimal calculateTotalConvertedBySeller(@Param("sellerId") Long sellerId);
}
