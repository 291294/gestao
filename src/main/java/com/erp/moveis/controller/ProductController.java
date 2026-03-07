package com.erp.moveis.controller;

import com.erp.moveis.model.Product;
import com.erp.moveis.service.ProductService;
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
@RequestMapping("/products")
@CrossOrigin(origins = "*")
@Tag(name = "Produtos", description = "Gestão de produtos")
@SecurityRequirement(name = "bearerAuth")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping
    @PreAuthorize("hasAuthority('product.list')")
    @Operation(summary = "Listar todos os produtos")
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product.view')")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<Optional<Product>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('product.create')")
    @Operation(summary = "Criar novo produto")
    public ResponseEntity<Product> create(@RequestBody Product product) {
        return ResponseEntity.ok(service.save(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    @Operation(summary = "Atualizar produto existente")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(service.update(id, product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product.delete')")
    @Operation(summary = "Deletar produto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}