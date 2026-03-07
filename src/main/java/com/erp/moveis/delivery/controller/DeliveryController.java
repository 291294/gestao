package com.erp.moveis.delivery.controller;

import com.erp.moveis.delivery.dto.DeliveryItemRequest;
import com.erp.moveis.delivery.dto.DeliveryRequest;
import com.erp.moveis.delivery.dto.DeliveryResponse;
import com.erp.moveis.delivery.entity.DeliveryStatus;
import com.erp.moveis.delivery.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
@Tag(name = "Entregas", description = "Gestão de entregas de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class DeliveryController {

    private final DeliveryService deliveryService;

    // ── CRUD ───────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar entrega")
    public ResponseEntity<DeliveryResponse> create(@RequestBody DeliveryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.createDelivery(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar entrega por ID (com itens)")
    public ResponseEntity<DeliveryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDelivery(id));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Listar entregas de um pedido")
    public ResponseEntity<List<DeliveryResponse>> findByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(deliveryService.getByOrder(orderId));
    }

    @GetMapping("/company/{companyId}/status/{status}")
    @Operation(summary = "Listar entregas por empresa e status")
    public ResponseEntity<List<DeliveryResponse>> findByCompanyAndStatus(
            @PathVariable Long companyId, @PathVariable DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.getByCompanyAndStatus(companyId, status));
    }

    @GetMapping("/company/{companyId}/in-transit")
    @Operation(summary = "Listar entregas em trânsito")
    public ResponseEntity<List<DeliveryResponse>> findInTransit(@PathVariable Long companyId) {
        return ResponseEntity.ok(deliveryService.getInTransit(companyId));
    }

    @GetMapping("/scheduled")
    @Operation(summary = "Listar entregas agendadas para uma data")
    public ResponseEntity<List<DeliveryResponse>> findScheduled(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(deliveryService.getScheduledForDate(date));
    }

    // ── Workflow ────────────────────────────────────────────────

    @PostMapping("/{id}/ship")
    @Operation(summary = "Despachar entrega (saída de estoque)")
    public ResponseEntity<DeliveryResponse> ship(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.ship(id));
    }

    @PostMapping("/{id}/deliver")
    @Operation(summary = "Marcar como entregue")
    public ResponseEntity<DeliveryResponse> deliver(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.deliver(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar entrega")
    public ResponseEntity<DeliveryResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.cancel(id));
    }

    // ── Itens ──────────────────────────────────────────────────

    @PostMapping("/{id}/items")
    @Operation(summary = "Adicionar item à entrega")
    public ResponseEntity<DeliveryResponse> addItem(
            @PathVariable Long id, @RequestBody DeliveryItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.addItem(id, request));
    }
}
