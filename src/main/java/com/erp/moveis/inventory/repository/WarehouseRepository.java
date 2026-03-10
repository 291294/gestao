package com.erp.moveis.inventory.repository;

import com.erp.moveis.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    List<Warehouse> findByCompanyId(Long companyId);
}
