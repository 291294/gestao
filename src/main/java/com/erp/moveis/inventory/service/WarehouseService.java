package com.erp.moveis.inventory.service;

import com.erp.moveis.inventory.entity.Warehouse;

import java.util.List;

public interface WarehouseService {

    Warehouse create(Warehouse warehouse);

    List<Warehouse> findByCompany(Long companyId);

    Warehouse findById(Long id);

    void deactivate(Long id);
}
