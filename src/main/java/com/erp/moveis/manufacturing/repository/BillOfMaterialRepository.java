package com.erp.moveis.manufacturing.repository;

import com.erp.moveis.manufacturing.entity.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {

    BillOfMaterial findByProductId(Long productId);
}
