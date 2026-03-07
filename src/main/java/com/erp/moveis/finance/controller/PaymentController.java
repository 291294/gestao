package com.erp.moveis.finance.controller;

import com.erp.moveis.finance.dto.PaymentRequest;
import com.erp.moveis.finance.dto.PaymentResponse;
import com.erp.moveis.finance.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamentos")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Criar pagamento")
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.created(URI.create("/api/payments/" + response.getId())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar pagamento por ID")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id));
    }

    @GetMapping("/invoice/{invoiceId}")
    @Operation(summary = "Listar pagamentos por nota fiscal")
    public ResponseEntity<List<PaymentResponse>> getByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getByInvoice(invoiceId));
    }

    @GetMapping("/company/{companyId}")
    @Operation(summary = "Listar pagamentos por empresa")
    public ResponseEntity<List<PaymentResponse>> getByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(paymentService.getByCompany(companyId));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirmar pagamento")
    public ResponseEntity<PaymentResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar pagamento")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancel(id));
    }

    @PostMapping("/{id}/refund")
    @Operation(summary = "Estornar pagamento")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    @GetMapping("/invoice/{invoiceId}/total")
    @Operation(summary = "Total confirmado por nota fiscal")
    public ResponseEntity<BigDecimal> getTotalByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getTotalConfirmedByInvoice(invoiceId));
    }

    @GetMapping("/company/{companyId}/revenue")
    @Operation(summary = "Receita confirmada por período")
    public ResponseEntity<BigDecimal> getRevenue(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(paymentService.getRevenueByPeriod(companyId, start, end));
    }

    @GetMapping("/company/{companyId}/period")
    @Operation(summary = "Pagamentos por empresa e período")
    public ResponseEntity<List<PaymentResponse>> getByPeriod(
            @PathVariable Long companyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(paymentService.getByCompanyAndPeriod(companyId, start, end));
    }
}
