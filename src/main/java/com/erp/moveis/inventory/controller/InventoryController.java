package com.erp.moveis.inventory.controller;

import com.erp.moveis.inventory.dto.InventoryItemRequest;
import com.erp.moveis.inventory.dto.InventoryItemResponse;
import com.erp.moveis.inventory.dto.InventoryMovementResponse;
import com.erp.moveis.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Tag(name = "Estoque", description = "Gestão de estoque e movimentações")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final InventoryService inventoryService;

    // ── CRUD ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar item de estoque")
    public ResponseEntity<InventoryItemResponse> create(@RequestBody InventoryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createItem(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar item por ID")
    public ResponseEntity<InventoryItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getItem(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de estoque")
    public ResponseEntity<InventoryItemResponse> update(@PathVariable Long id, @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(inventoryService.updateItem(id, request));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar estoque por empresa")
    public ResponseEntity<List<InventoryItemResponse>> findByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(inventoryService.getByCompany(companyId));
    }

    @GetMapping("/company/{companyId}/product/{productId}")
    @Operation(summary = "Buscar estoque de um produto")
    public ResponseEntity<InventoryItemResponse> findByProduct(
            @PathVariable Long companyId, @PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProduct(companyId, productId));
    }

    @GetMapping("/company/{companyId}/low-stock")
    @Operation(summary = "Listar itens com estoque baixo")
    public ResponseEntity<List<InventoryItemResponse>> findLowStock(@PathVariable Long companyId) {
        return ResponseEntity.ok(inventoryService.getLowStock(companyId));
    }

    @GetMapping("/company/{companyId}/out-of-stock")
    @Operation(summary = "Listar itens sem estoque")
    public ResponseEntity<List<InventoryItemResponse>> findOutOfStock(@PathVariable Long companyId) {
        return ResponseEntity.ok(inventoryService.getOutOfStock(companyId));
    }

    // ── Movimentações ──────────────────────────────────────────

    @PostMapping("/{id}/add")
    @Operation(summary = "Entrada de estoque")
    public ResponseEntity<InventoryMovementResponse> addStock(
            @PathVariable Long id,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(inventoryService.addStock(id, quantity, referenceType, referenceId, notes));
    }

    @PostMapping("/{id}/remove")
    @Operation(summary = "Saída de estoque")
    public ResponseEntity<InventoryMovementResponse> removeStock(
            @PathVariable Long id,
            @RequestParam int quantity,
            @RequestParam(required = false) String referenceType,
            @RequestParam(required = false) Long referenceId,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(inventoryService.removeStock(id, quantity, referenceType, referenceId, notes));
    }

    @PostMapping("/{id}/adjust")
    @Operation(summary = "Ajustar estoque manualmente")
    public ResponseEntity<InventoryMovementResponse> adjustStock(
            @PathVariable Long id,
            @RequestParam int newQuantity,
            @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(inventoryService.adjustStock(id, newQuantity, notes));
    }

    @PostMapping("/{id}/reserve")
    @Operation(summary = "Reservar estoque")
    public ResponseEntity<InventoryItemResponse> reserve(
            @PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.reserveStock(id, quantity));
    }

    @PostMapping("/{id}/release")
    @Operation(summary = "Liberar reserva de estoque")
    public ResponseEntity<InventoryItemResponse> release(
            @PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(inventoryService.releaseReservation(id, quantity));
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Histórico de movimentações")
    public ResponseEntity<List<InventoryMovementResponse>> movements(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getMovements(id));
    }

    // ── Warehouse-aware stock ───────────────────────────────────

    @GetMapping("/stock")
    @Operation(summary = "Consultar estoque por produto e armazém")
    public ResponseEntity<BigDecimal> getWarehouseStock(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {
        return ResponseEntity.ok(inventoryService.getWarehouseStock(productId, warehouseId));
    }

    @PostMapping("/stock/add")
    @Operation(summary = "Entrada de estoque por armazém")
    public ResponseEntity<Void> addWarehouseStock(
            @RequestParam Long companyId,
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam BigDecimal quantity) {
        inventoryService.addWarehouseStock(companyId, productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock/remove")
    @Operation(summary = "Saída de estoque por armazém")
    public ResponseEntity<Void> removeWarehouseStock(
            @RequestParam Long companyId,
            @RequestParam Long productId,
            @RequestParam Long warehouseId,
            @RequestParam BigDecimal quantity) {
        inventoryService.removeWarehouseStock(companyId, productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }
}
