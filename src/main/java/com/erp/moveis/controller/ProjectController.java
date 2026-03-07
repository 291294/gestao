package com.erp.moveis.controller;

import com.erp.moveis.model.Project;
import com.erp.moveis.service.ProjectService;
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
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
@Tag(name = "Projetos", description = "Gestão de projetos")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectService service;

    @GetMapping
    @PreAuthorize("hasAuthority('project.list')")
    @Operation(summary = "Listar todos os projetos")
    public ResponseEntity<List<Project>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('project.view')")
    @Operation(summary = "Buscar projeto por ID")
    public ResponseEntity<Optional<Project>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAuthority('project.list')")
    @Operation(summary = "Buscar projetos por cliente")
    public ResponseEntity<List<Project>> findByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(service.findByClientId(clientId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('project.create')")
    @Operation(summary = "Criar novo projeto")
    public ResponseEntity<Project> create(@RequestBody Project project) {
        return ResponseEntity.ok(service.save(project));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('project.update')")
    @Operation(summary = "Atualizar projeto existente")
    public ResponseEntity<Project> update(@PathVariable Long id, @RequestBody Project project) {
        return ResponseEntity.ok(service.update(id, project));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('project.delete')")
    @Operation(summary = "Deletar projeto")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}