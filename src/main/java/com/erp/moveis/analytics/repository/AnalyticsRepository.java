package com.erp.moveis.analytics.repository;

import com.erp.moveis.model.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface AnalyticsRepository extends Repository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.companyId = :companyId")
    Long countOrders(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(o.totalValue), 0) FROM Order o WHERE o.companyId = :companyId")
    Double totalRevenue(@Param("companyId") Long companyId);
}
