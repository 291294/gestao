package com.erp.moveis.controller;

import com.erp.moveis.model.Supplier;
import com.erp.moveis.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/suppliers")
@Tag(name = "Fornecedores", description = "Gestão de fornecedores")
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

    @Autowired
    private SupplierService service;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('supplier.list', 'client.list')")
    @Operation(summary = "Listar todos os fornecedores")
    public ResponseEntity<List<Supplier>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyAuthority('supplier.list', 'client.list')")
    @Operation(summary = "Listar fornecedores ativos")
    public ResponseEntity<List<Supplier>> listActive() {
        return ResponseEntity.ok(service.listActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier.list', 'client.list')")
    @Operation(summary = "Buscar fornecedor por ID")
    public ResponseEntity<Supplier> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('supplier.create', 'client.create')")
    @Operation(summary = "Criar fornecedor")
    public ResponseEntity<Supplier> create(@RequestBody Supplier supplier) {
        return ResponseEntity.ok(service.save(supplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier.update', 'client.update')")
    @Operation(summary = "Atualizar fornecedor")
    public ResponseEntity<Supplier> update(@PathVariable Long id, @RequestBody Supplier supplier) {
        return ResponseEntity.ok(service.update(id, supplier));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('supplier.delete', 'client.delete')")
    @Operation(summary = "Excluir fornecedor")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
