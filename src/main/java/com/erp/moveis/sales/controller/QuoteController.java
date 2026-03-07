package com.erp.moveis.sales.controller;

import com.erp.moveis.sales.dto.QuoteItemRequest;
import com.erp.moveis.sales.dto.QuoteRequest;
import com.erp.moveis.sales.dto.QuoteResponse;
import com.erp.moveis.sales.entity.Quote;
import com.erp.moveis.sales.entity.QuoteItem;
import com.erp.moveis.sales.entity.QuoteStatus;
import com.erp.moveis.sales.mapper.QuoteMapper;
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
import java.util.stream.Collectors;

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
        Quote quote = QuoteMapper.toEntity(request);
        Quote saved = quoteService.createQuote(quote);
        return ResponseEntity.status(HttpStatus.CREATED).body(QuoteMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar orçamento por ID (com itens)")
    public ResponseEntity<QuoteResponse> findById(@PathVariable Long id) {
        Quote quote = quoteService.findFullQuote(id);
        return ResponseEntity.ok(QuoteMapper.toResponse(quote));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar orçamentos por empresa (paginado)")
    public ResponseEntity<Page<QuoteResponse>> findByCompany(
            @PathVariable Long companyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Quote> quotes = quoteService.findByCompany(
                companyId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
        return ResponseEntity.ok(quotes.map(QuoteMapper::toResponse));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar orçamentos por status")
    public ResponseEntity<List<QuoteResponse>> findByStatus(@PathVariable QuoteStatus status) {
        List<QuoteResponse> list = quoteService.findByStatus(status).stream()
                .map(QuoteMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
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
        Quote quote = quoteService.updateStatus(id, status);
        return ResponseEntity.ok(QuoteMapper.toResponse(quote));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Aprovar orçamento")
    public ResponseEntity<QuoteResponse> approve(@PathVariable Long id) {
        Quote quote = quoteService.approve(id);
        return ResponseEntity.ok(QuoteMapper.toResponse(quote));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Rejeitar orçamento")
    public ResponseEntity<QuoteResponse> reject(@PathVariable Long id) {
        Quote quote = quoteService.reject(id);
        return ResponseEntity.ok(QuoteMapper.toResponse(quote));
    }

    // ── Itens ──────────────────────────────────────────────────

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar item ao orçamento")
    public ResponseEntity<QuoteResponse> addItem(
            @PathVariable Long id,
            @RequestBody QuoteItemRequest request) {
        QuoteItem item = QuoteMapper.toItemEntity(request);
        Quote quote = quoteService.addItem(id, item);
        return ResponseEntity.status(HttpStatus.CREATED).body(QuoteMapper.toResponse(quote));
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
        Quote quote = quoteService.calculateTotals(id);
        return ResponseEntity.ok(QuoteMapper.toResponse(quote));
    }
}
