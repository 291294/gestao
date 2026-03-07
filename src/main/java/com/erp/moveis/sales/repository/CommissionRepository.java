package com.erp.moveis.sales.repository;

import com.erp.moveis.sales.model.Commission;
import com.erp.moveis.sales.model.Commission.CommissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionRepository extends JpaRepository<Commission, Long> {

    List<Commission> findBySellerId(Long sellerId);

    List<Commission> findBySellerIdAndStatus(Long sellerId, CommissionStatus status);

    Optional<Commission> findByOrderId(Long orderId);

    List<Commission> findByStatus(CommissionStatus status);

    @Query("SELECT c FROM Commission c WHERE c.seller.id = :sellerId AND c.paymentDate BETWEEN :startDate AND :endDate")
    List<Commission> findBySellerAndPaymentPeriod(
            @Param("sellerId") Long sellerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM Commission c WHERE c.seller.id = :sellerId AND c.status = 'PAID'")
    java.math.BigDecimal calculateTotalPaidBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM Commission c WHERE c.seller.id = :sellerId AND c.status = 'PENDING'")
    java.math.BigDecimal calculateTotalPendingBySeller(@Param("sellerId") Long sellerId);

    @Query("SELECT c FROM Commission c WHERE c.status = 'APPROVED' AND c.paymentDate <= :date")
    List<Commission> findDueForPayment(@Param("date") LocalDate date);
}
