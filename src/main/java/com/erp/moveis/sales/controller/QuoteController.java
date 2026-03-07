package com.erp.moveis.sales.controller;

import com.erp.moveis.sales.dto.QuoteItemRequest;
import com.erp.moveis.sales.dto.QuoteRequest;
import com.erp.moveis.sales.dto.QuoteResponse;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.service.QuoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
@Tag(name = "Orçamentos", description = "Gestão de orçamentos de vendas")
@SecurityRequirement(name = "bearerAuth")
public class QuoteController {

    private final QuoteService quoteService;

    // ── CRUD ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar novo orçamento")
    public ResponseEntity<QuoteResponse> create(@RequestBody QuoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quoteService.createQuote(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID (com itens)")
    public ResponseEntity<QuoteResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.getQuote(id));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar orçamentos por empresa (paginado)")
    public ResponseEntity<Page<QuoteResponse>> findByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(quoteService.getQuotesByCompany(
                companyId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        ));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar orçamentos por status")
    public ResponseEntity<List<QuoteResponse>> findByStatus(@PathVariable QuoteStatus status) {
        return ResponseEntity.ok(quoteService.getQuotesByStatus(status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar orçamento")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quoteService.deleteQuote(id);
        return ResponseEntity.noContent().build();
    }

    // ── Workflow ────────────────────────────────────────────────

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do orçamento")
    public ResponseEntity<QuoteResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam QuoteStatus status) {
        return ResponseEntity.ok(quoteService.updateStatus(id, status));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprovar orçamento")
    public ResponseEntity<QuoteResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.approve(id));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rejeitar orçamento")
    public ResponseEntity<QuoteResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.reject(id));
    }

    // ── Itens ──────────────────────────────────────────────────

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar item ao orçamento")
    public ResponseEntity<QuoteResponse> addItem(
            @PathVariable Long id,
            @RequestBody QuoteItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(quoteService.addItem(id, request));
    }

    @DeleteMapping("/{quoteId}/items/{itemId}")
    @Operation(summary = "Remover item do orçamento")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long quoteId,
            @PathVariable Long itemId) {
        quoteService.removeItem(quoteId, itemId);
        return ResponseEntity.noContent().build();
    }

    // ── Cálculos ───────────────────────────────────────────────

    @PostMapping("/{id}/calculate")
    @Operation(summary = "Recalcular totais do orçamento")
    public ResponseEntity<QuoteResponse> calculate(@PathVariable Long id) {
        return ResponseEntity.ok(quoteService.calculateTotals(id));
    }
}
