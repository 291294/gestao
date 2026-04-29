package com.erp.moveis.manufacturing.repository;

import com.erp.moveis.manufacturing.entity.BillOfMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterial, Long> {

    Optional<BillOfMaterial> findByProductIdAndCompanyId(Long productId, Long companyId);
}
