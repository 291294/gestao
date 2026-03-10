package com.erp.moveis.inventory.service;

import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.entity.Warehouse;
import com.erp.moveis.inventory.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository repository;

    @Override
    public Warehouse create(Warehouse warehouse) {
        warehouse.setCreatedAt(LocalDateTime.now());
        warehouse.setActive(true);
        return repository.save(warehouse);
    }

    @Override
    public List<Warehouse> findByCompany(Long companyId) {
        return repository.findByCompanyId(companyId);
    }

    @Override
    public Warehouse findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
    }

    @Override
    public void deactivate(Long id) {
        Warehouse warehouse = findById(id);
        warehouse.setActive(false);
        repository.save(warehouse);
    }
}
