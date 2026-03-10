package com.erp.moveis.inventory.controller;

import com.erp.moveis.inventory.entity.Warehouse;
import com.erp.moveis.inventory.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService service;

    @PostMapping
    public Warehouse create(@RequestBody Warehouse warehouse) {
        return service.create(warehouse);
    }

    @GetMapping("/company/{companyId}")
    public List<Warehouse> list(@PathVariable Long companyId) {
        return service.findByCompany(companyId);
    }

    @GetMapping("/{id}")
    public Warehouse get(@PathVariable Long id) {
        return service.findById(id);
    }

    @DeleteMapping("/{id}")
    public void deactivate(@PathVariable Long id) {
        service.deactivate(id);
    }
}
