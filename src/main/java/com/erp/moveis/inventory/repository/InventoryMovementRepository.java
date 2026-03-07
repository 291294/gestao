package com.erp.moveis.inventory.repository;

import com.erp.moveis.inventory.entity.InventoryMovement;
import com.erp.moveis.inventory.entity.MovementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByInventoryItemIdOrderByCreatedAtDesc(Long inventoryItemId);

    List<InventoryMovement> findByReferenceTypeAndReferenceId(String referenceType, Long referenceId);

    List<InventoryMovement> findByCompanyIdAndMovementType(Long companyId, MovementType type);
}
