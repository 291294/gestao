package com.erp.moveis.controller;

import com.erp.moveis.dto.OrderRequest;
import com.erp.moveis.dto.OrderResponse;
import com.erp.moveis.dto.PageResponse;
import com.erp.moveis.mapper.OrderMapper;
import com.erp.moveis.model.Order;
import com.erp.moveis.model.OrderItem;
import com.erp.moveis.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "Gestão de pedidos")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('order.list')")
    @Operation(summary = "Listar todos os pedidos")
    public ResponseEntity<List<OrderResponse>> list() {
        return ResponseEntity.ok(service.list().stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('order.list')")
    @Operation(summary = "Listar pedidos paginados")
    public ResponseEntity<PageResponse<OrderResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort));
        var result = service.listPaged(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order.view')")
    @Operation(summary = "Buscar pedido por ID")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAuthority('order.list')")
    @Operation(summary = "Buscar pedidos por cliente")
    public ResponseEntity<List<OrderResponse>> findByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(service.findByClientId(clientId).stream().map(mapper::toResponse).toList());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('order.create')")
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        Order order = mapper.toEntity(request);
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                OrderItem item = mapper.toItemEntity(itemReq);
                item.setOrder(order);
                order.getItems().add(item);
            }
        }
        Order saved = service.save(order);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('order.update')")
    @Operation(summary = "Atualizar pedido existente")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @RequestBody Order order) {
        return ResponseEntity.ok(mapper.toResponse(service.update(id, order)));
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
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(mapper.toResponse(service.cancel(id)));
    }
}