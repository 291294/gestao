package com.erp.moveis.inventory.repository;

import com.erp.moveis.inventory.entity.StockMovement;
import com.erp.moveis.inventory.entity.StockMovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
        SELECT COALESCE(SUM(sm.quantity), 0)
        FROM StockMovement sm
        WHERE sm.productId = :productId
        AND sm.warehouseId = :warehouseId
    """)
    BigDecimal getCurrentStock(@Param("productId") Long productId,
                               @Param("warehouseId") Long warehouseId);

    @Query("""
        SELECT COALESCE(SUM(sm.quantity), 0)
        FROM StockMovement sm
        WHERE sm.productId = :productId
        AND sm.companyId = :companyId
    """)
    BigDecimal getTotalStockByProduct(@Param("companyId") Long companyId,
                                      @Param("productId") Long productId);

    List<StockMovement> findByProductIdAndWarehouseIdOrderByCreatedAtDesc(Long productId, Long warehouseId);

    List<StockMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    List<StockMovement> findByCompanyIdAndMovementType(Long companyId, StockMovementType movementType);
}
