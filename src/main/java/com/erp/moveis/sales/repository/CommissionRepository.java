package com.erp.moveis.sales.repository;

import com.erp.moveis.sales.entity.Commission;
import com.erp.moveis.sales.entity.Commission.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {

    Optional<Commission> findByIdAndCompanyId(Long id, Long companyId);

    List<Commission> findByCompanyId(Long companyId);

    List<Commission> findBySellerId(Long sellerId);

    List<Commission> findBySellerIdAndStatus(Long sellerId, CommissionStatus status);

    Optional<Commission> findByOrderId(Long orderId);

    List<Commission> findByStatus(CommissionStatus status);

    @Query("SELECT c FROM Commission c WHERE c.companyId = :companyId AND c.sellerId = :sellerId AND c.paymentDate BETWEEN :startDate AND :endDate")
    List<Commission> findBySellerAndPaymentPeriod(
            @Param("companyId") Long companyId,
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM Commission c WHERE c.companyId = :companyId AND c.sellerId = :sellerId AND c.status = 'PAID'")
    java.math.BigDecimal calculateTotalPaidBySeller(@Param("companyId") Long companyId, @Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM Commission c WHERE c.companyId = :companyId AND c.sellerId = :sellerId AND c.status = 'PENDING'")
    java.math.BigDecimal calculateTotalPendingBySeller(@Param("companyId") Long companyId, @Param("sellerId") Long sellerId);

    @Query("SELECT c FROM Commission c WHERE c.companyId = :companyId AND c.status = 'APPROVED' AND c.paymentDate <= :date")
    List<Commission> findDueForPayment(@Param("companyId") Long companyId, @Param("date") LocalDate date);
}
