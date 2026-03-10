package com.erp.moveis.analytics.repository;

import com.erp.moveis.model.Order;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnalyticsRepository extends Repository<Order, Long> {

    @Query("SELECT COUNT(o) FROM Order o WHERE o.companyId = :companyId")
    Long countOrders(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(o.totalValue), 0) FROM Order o WHERE o.companyId = :companyId")
    Double totalRevenue(@Param("companyId") Long companyId);

    @Query("SELECT COUNT(c) FROM Client c")
    Long countClients();

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.companyId = :companyId GROUP BY o.status")
    List<Object[]> countOrdersByStatus(@Param("companyId") Long companyId);

    @Query(value = "SELECT TO_CHAR(TO_TIMESTAMP(o.created_at / 1000), 'YYYY-MM') AS month, " +
            "COALESCE(SUM(o.total_value), 0) AS revenue " +
            "FROM orders o WHERE o.company_id = :companyId AND o.created_at IS NOT NULL " +
            "GROUP BY month ORDER BY month DESC LIMIT 6", nativeQuery = true)
    List<Object[]> revenueByMonth(@Param("companyId") Long companyId);
}
