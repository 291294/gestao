package com.erp.moveis.core.company.controller;

import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.company.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/companies")
@Tag(name = "Empresas", description = "Configuracao da empresa")
@SecurityRequirement(name = "bearerAuth")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('company.list')")
    @Operation(summary = "Listar empresas")
    public ResponseEntity<List<Company>> list() {
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('company.view')")
    @Operation(summary = "Buscar empresa por ID")
    public ResponseEntity<Company> findById(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('company.update')")
    @Operation(summary = "Atualizar empresa")
    public ResponseEntity<Company> update(@PathVariable Long id, @RequestBody Company company) {
        return companyRepository.findById(id).map(existing -> {
            existing.setName(company.getName());
            existing.setCnpj(company.getCnpj());
            return ResponseEntity.ok(companyRepository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }
}
