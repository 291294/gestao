package com.erp.moveis.sales.repository;

import com.erp.moveis.sales.entity.SalesTarget;
import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesTargetRepository extends JpaRepository<SalesTarget, Long> {

    List<SalesTarget> findBySellerIdAndStatus(Long sellerId, TargetStatus status);

    List<SalesTarget> findByCompanyIdAndTargetType(Long companyId, TargetType targetType);

    @Query("SELECT st FROM SalesTarget st WHERE st.sellerId = :sellerId AND :date BETWEEN st.periodStart AND st.periodEnd AND st.status = 'ACTIVE'")
    List<SalesTarget> findActiveTargetsForSellerAtDate(
            @Param("sellerId") Long sellerId,
            @Param("date") LocalDate date
    );

    @Query("SELECT st FROM SalesTarget st WHERE st.companyId = :companyId AND :date BETWEEN st.periodStart AND st.periodEnd AND st.status = 'ACTIVE'")
    List<SalesTarget> findActiveTargetsForCompanyAtDate(
            @Param("companyId") Long companyId,
            @Param("date") LocalDate date
    );

    @Query("SELECT st FROM SalesTarget st WHERE st.periodEnd < :date AND st.status = 'ACTIVE'")
    List<SalesTarget> findExpiredTargets(@Param("date") LocalDate date);

    List<SalesTarget> findByStatus(TargetStatus status);
}
