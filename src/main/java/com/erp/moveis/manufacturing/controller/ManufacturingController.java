package com.erp.moveis.manufacturing.controller;

import com.erp.moveis.manufacturing.entity.ProductionOrder;
import com.erp.moveis.manufacturing.service.ManufacturingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/manufacturing")
@RequiredArgsConstructor
public class ManufacturingController {

    private final ManufacturingService service;

    @PostMapping("/production-order")
    public ProductionOrder create(
            @RequestParam Long companyId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity
    ) {
        return service.createProductionOrder(companyId, productId, quantity);
    }

    @PostMapping("/production-order/{id}/start")
    public void start(@PathVariable Long id) {
        service.startProduction(id);
    }

    @PostMapping("/production-order/{id}/finish")
    public void finish(@PathVariable Long id) {
        service.finishProduction(id);
    }
}
