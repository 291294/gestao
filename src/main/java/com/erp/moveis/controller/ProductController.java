package com.erp.moveis.controller;

import com.erp.moveis.dto.PageResponse;
import com.erp.moveis.dto.ProductRequest;
import com.erp.moveis.dto.ProductResponse;
import com.erp.moveis.mapper.ProductMapper;
import com.erp.moveis.model.Product;
import com.erp.moveis.service.ProductService;
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
@RequestMapping("/products")
@CrossOrigin(origins = "*")
@Tag(name = "Produtos", description = "Gestão de produtos")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;
    private final ProductMapper mapper;

    @GetMapping
    @PreAuthorize("hasAuthority('product.list')")
    @Operation(summary = "Listar todos os produtos")
    public ResponseEntity<List<ProductResponse>> list() {
        return ResponseEntity.ok(service.list().stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('product.list')")
    @Operation(summary = "Listar produtos paginados")
    public ResponseEntity<PageResponse<ProductResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort) {
        var pageable = PageRequest.of(page, size, Sort.by(sort));
        var result = service.listPaged(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product.view')")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('product.create')")
    @Operation(summary = "Criar novo produto")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        Product entity = mapper.toEntity(request);
        Product saved = service.save(entity);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('product.update')")
    @Operation(summary = "Atualizar produto existente")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return service.findById(id).map(existing -> {
            mapper.updateEntity(request, existing);
            Product saved = service.save(existing);
            return ResponseEntity.ok(mapper.toResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product.delete')")
    @Operation(summary = "Deletar produto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}