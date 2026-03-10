package com.erp.moveis.inventory.repository;

import com.erp.moveis.inventory.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByCompanyIdAndProductId(Long companyId, Long productId);

    List<InventoryItem> findByCompanyId(Long companyId);

    @Query("SELECT i FROM InventoryItem i WHERE i.companyId = :companyId AND (i.quantityOnHand - i.quantityReserved) <= i.minStockLevel")
    List<InventoryItem> findLowStock(@Param("companyId") Long companyId);

    @Query("SELECT i FROM InventoryItem i WHERE i.companyId = :companyId AND i.quantityOnHand = 0")
    List<InventoryItem> findOutOfStock(@Param("companyId") Long companyId);

    @Query("SELECT i FROM InventoryItem i WHERE (i.quantityOnHand - i.quantityReserved) <= i.minStockLevel")
    List<InventoryItem> findAllLowStock();
}
