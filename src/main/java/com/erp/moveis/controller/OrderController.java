package com.erp.moveis.controller;

import com.erp.moveis.model.Order;
import com.erp.moveis.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Gestão de pedidos")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    @Autowired
    private OrderService service;

    @GetMapping
    @PreAuthorize("hasAuthority('order.list')")
    @Operation(summary = "Listar todos os pedidos")
    public ResponseEntity<List<Order>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order.view')")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<Optional<Order>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAuthority('order.list')")
    @Operation(summary = "Buscar pedidos por cliente")
    public ResponseEntity<List<Order>> findByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(service.findByClientId(clientId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order.create')")
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<Order> create(@RequestBody Order order) {
        return ResponseEntity.ok(service.save(order));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order.update')")
    @Operation(summary = "Atualizar pedido existente")
    public ResponseEntity<Order> update(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.ok(service.update(id, order));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('order.delete')")
    @Operation(summary = "Deletar pedido")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('order.update')")
    @Operation(summary = "Cancelar pedido e liberar estoque reservado")
    public ResponseEntity<Order> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(id));
    }
}