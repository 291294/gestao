package com.erp.moveis.sales.controller;

import com.erp.moveis.sales.dto.CommissionRequest;
import com.erp.moveis.sales.dto.CommissionResponse;
import com.erp.moveis.sales.entity.Commission.CommissionStatus;
import com.erp.moveis.sales.service.CommissionService;
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
@RequestMapping("/commissions")
@RequiredArgsConstructor
@Tag(name = "Comissões", description = "Gestão de comissões de vendedores")
@SecurityRequirement(name = "bearerAuth")
public class CommissionController {

    private final CommissionService commissionService;

    // ── CRUD ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar comissão manualmente")
    public ResponseEntity<CommissionResponse> create(@RequestBody CommissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commissionService.createCommission(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar comissão por ID")
    public ResponseEntity<CommissionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(commissionService.getCommission(id));
    }

    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Listar comissões de um vendedor")
    public ResponseEntity<List<CommissionResponse>> findBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(commissionService.getCommissionsBySeller(sellerId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar comissões por status")
    public ResponseEntity<List<CommissionResponse>> findByStatus(@PathVariable CommissionStatus status) {
        return ResponseEntity.ok(commissionService.getCommissionsByStatus(status));
    }

    @GetMapping("/seller/{sellerId}/pending")
    @Operation(summary = "Listar comissões pendentes de um vendedor")
    public ResponseEntity<List<CommissionResponse>> findPending(@PathVariable Long sellerId) {
        return ResponseEntity.ok(commissionService.getPendingBySeller(sellerId));
    }

    @GetMapping("/due")
    @Operation(summary = "Listar comissões aprovadas com pagamento vencido")
    public ResponseEntity<List<CommissionResponse>> findDue() {
        return ResponseEntity.ok(commissionService.getDueForPayment());
    }

    // ── Workflow ────────────────────────────────────────────────

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprovar comissão")
    public ResponseEntity<CommissionResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(commissionService.approve(id));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "Marcar comissão como paga")
    public ResponseEntity<CommissionResponse> pay(@PathVariable Long id) {
        return ResponseEntity.ok(commissionService.pay(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar comissão")
    public ResponseEntity<CommissionResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(commissionService.cancel(id));
    }

    // ── Relatórios ─────────────────────────────────────────────

    @GetMapping("/seller/{sellerId}/total-paid")
    @Operation(summary = "Total de comissões pagas de um vendedor")
    public ResponseEntity<BigDecimal> totalPaid(@PathVariable Long sellerId) {
        return ResponseEntity.ok(commissionService.getTotalPaidBySeller(sellerId));
    }

    @GetMapping("/seller/{sellerId}/total-pending")
    @Operation(summary = "Total de comissões pendentes de um vendedor")
    public ResponseEntity<BigDecimal> totalPending(@PathVariable Long sellerId) {
        return ResponseEntity.ok(commissionService.getTotalPendingBySeller(sellerId));
    }
}
