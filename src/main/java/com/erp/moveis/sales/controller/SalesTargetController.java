package com.erp.moveis.sales.controller;

import com.erp.moveis.sales.dto.SalesTargetRequest;
import com.erp.moveis.sales.dto.SalesTargetResponse;
import com.erp.moveis.sales.entity.SalesTarget.TargetStatus;
import com.erp.moveis.sales.entity.SalesTarget.TargetType;
import com.erp.moveis.sales.service.SalesTargetService;
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
@RequestMapping("/sales-targets")
@RequiredArgsConstructor
@Tag(name = "Metas de Vendas", description = "Gestão de metas de vendas")
@SecurityRequirement(name = "bearerAuth")
public class SalesTargetController {

    private final SalesTargetService salesTargetService;

    // ── CRUD ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar nova meta de vendas")
    public ResponseEntity<SalesTargetResponse> create(@RequestBody SalesTargetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(salesTargetService.createTarget(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta por ID")
    public ResponseEntity<SalesTargetResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(salesTargetService.getTarget(id));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Listar metas de um vendedor por status")
    public ResponseEntity<List<SalesTargetResponse>> findBySeller(
            @PathVariable Long sellerId,
            @RequestParam TargetStatus status) {
        return ResponseEntity.ok(salesTargetService.getTargetsBySeller(sellerId, status));
    }

    @GetMapping("/seller/{sellerId}/active")
    @Operation(summary = "Listar metas ativas de um vendedor")
    public ResponseEntity<List<SalesTargetResponse>> findActiveBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(salesTargetService.getActiveTargetsForSeller(sellerId));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar metas por empresa e tipo")
    public ResponseEntity<List<SalesTargetResponse>> findByCompany(
            @PathVariable Long companyId,
            @RequestParam TargetType type) {
        return ResponseEntity.ok(salesTargetService.getTargetsByCompany(companyId, type));
    }

    // ── Progresso ──────────────────────────────────────────────

    @PostMapping("/{id}/add-achieved")
    @Operation(summary = "Adicionar valor atingido à meta")
    public ResponseEntity<SalesTargetResponse> addAchieved(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(salesTargetService.addAchievedAmount(id, amount));
    }

    // ── Workflow ────────────────────────────────────────────────

    @PostMapping("/{id}/complete")
    @Operation(summary = "Completar meta manualmente")
    public ResponseEntity<SalesTargetResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(salesTargetService.complete(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar meta")
    public ResponseEntity<SalesTargetResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(salesTargetService.cancel(id));
    }

    @PostMapping("/close-expired")
    @Operation(summary = "Fechar todas as metas expiradas")
    public ResponseEntity<Void> closeExpired() {
        salesTargetService.closeExpiredTargets();
        return ResponseEntity.ok().build();
    }
}
