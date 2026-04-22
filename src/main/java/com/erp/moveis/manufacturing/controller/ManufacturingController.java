package com.erp.moveis.manufacturing.controller;

import com.erp.moveis.manufacturing.entity.ProductionOrder;
import com.erp.moveis.manufacturing.service.ManufacturingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/manufacturing")
@RequiredArgsConstructor
@Tag(name = "Produção", description = "Gestão de ordens de produção")
@SecurityRequirement(name = "bearerAuth")
public class ManufacturingController {

    private final ManufacturingService service;

    @GetMapping("/production-orders")
    @PreAuthorize("hasAnyAuthority('report.production', 'order.list')")
    @Operation(summary = "Listar todas as ordens de produção")
    public List<ProductionOrder> listAll() {
        return service.findByCompany(null);
    }

    @GetMapping("/production-orders/company/{companyId}")
    @PreAuthorize("hasAnyAuthority('report.production', 'order.list')")
    @Operation(summary = "Listar ordens de produção por empresa")
    public List<ProductionOrder> listByCompany(@PathVariable Long companyId) {
        return service.findByCompany(companyId);
    }

    @PostMapping("/production-order")
    @PreAuthorize("hasAnyAuthority('report.production', 'order.create')")
    @Operation(summary = "Criar ordem de produção")
    public ProductionOrder create(
            @RequestParam Long companyId,
            @RequestParam Long productId,
            @RequestParam BigDecimal quantity
    ) {
        return service.createProductionOrder(companyId, productId, quantity);
    }

    @PostMapping("/production-order/{id}/start")
    @PreAuthorize("hasAnyAuthority('report.production', 'order.update')")
    @Operation(summary = "Iniciar ordem de produção")
    public void start(@PathVariable Long id) {
        service.startProduction(id);
    }

    @PostMapping("/production-order/{id}/finish")
    @PreAuthorize("hasAnyAuthority('report.production', 'order.update')")
    @Operation(summary = "Finalizar ordem de produção")
    public void finish(@PathVariable Long id) {
        service.finishProduction(id);
    }
}
