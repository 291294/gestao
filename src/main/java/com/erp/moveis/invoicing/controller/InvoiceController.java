package com.erp.moveis.invoicing.controller;

import com.erp.moveis.invoicing.dto.InvoiceItemRequest;
import com.erp.moveis.invoicing.dto.InvoiceRequest;
import com.erp.moveis.invoicing.dto.InvoiceResponse;
import com.erp.moveis.invoicing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@Tag(name = "Notas Fiscais")
@SecurityRequirement(name = "bearerAuth")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @Operation(summary = "Criar nota fiscal")
    public ResponseEntity<InvoiceResponse> create(@RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.createInvoice(request);
        return ResponseEntity.created(URI.create("/api/invoices/" + response.getId())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota fiscal por ID")
    public ResponseEntity<InvoiceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoice(id));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar notas fiscais por empresa (paginado)")
    public ResponseEntity<Page<InvoiceResponse>> getByCompany(@PathVariable Long companyId, Pageable pageable) {
        return ResponseEntity.ok(invoiceService.getByCompany(companyId, pageable));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar notas fiscais por cliente")
    public ResponseEntity<List<InvoiceResponse>> getByClient(@PathVariable Long clientId) {
        return ResponseEntity.ok(invoiceService.getByClient(clientId));
    }

    @PostMapping("/{id}/issue")
    @Operation(summary = "Emitir nota fiscal")
    public ResponseEntity<InvoiceResponse> issue(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.issue(id));
    }

    @PostMapping("/{id}/send")
    @Operation(summary = "Enviar nota fiscal")
    public ResponseEntity<InvoiceResponse> send(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.send(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar nota fiscal")
    public ResponseEntity<InvoiceResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.cancel(id));
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Registrar pagamento na nota fiscal")
    public ResponseEntity<InvoiceResponse> registerPayment(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(invoiceService.registerPayment(id, amount));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar item à nota fiscal")
    public ResponseEntity<InvoiceResponse> addItem(@PathVariable Long id, @RequestBody InvoiceItemRequest request) {
        return ResponseEntity.ok(invoiceService.addItem(id, request));
    }

    @PostMapping("/{id}/calculate")
    @Operation(summary = "Recalcular totais da nota fiscal")
    public ResponseEntity<InvoiceResponse> calculateTotals(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.calculateTotals(id));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Listar notas fiscais vencidas")
    public ResponseEntity<List<InvoiceResponse>> getOverdue() {
        return ResponseEntity.ok(invoiceService.getOverdue());
    }

    @GetMapping("/client/{clientId}/balance")
    @Operation(summary = "Saldo em aberto por cliente")
    public ResponseEntity<BigDecimal> getOpenBalance(@PathVariable Long clientId) {
        return ResponseEntity.ok(invoiceService.getOpenBalanceByClient(clientId));
    }

    @GetMapping("/company/{companyId}/revenue")
    @Operation(summary = "Receita total por empresa")
    public ResponseEntity<BigDecimal> getTotalRevenue(@PathVariable Long companyId) {
        return ResponseEntity.ok(invoiceService.getTotalRevenue(companyId));
    }

    @PostMapping("/from-order/{orderId}")
    @Operation(summary = "Criar nota fiscal a partir de um pedido")
    public ResponseEntity<InvoiceResponse> createFromOrder(
            @PathVariable Long orderId,
            @RequestParam Long companyId,
            @RequestParam Long clientId) {
        InvoiceResponse response = invoiceService.createFromOrder(orderId, companyId, clientId);
        return ResponseEntity.created(URI.create("/api/invoices/" + response.getId())).body(response);
    }
}
