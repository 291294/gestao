package com.erp.moveis.manufacturing.repository;

import com.erp.moveis.manufacturing.entity.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
}
