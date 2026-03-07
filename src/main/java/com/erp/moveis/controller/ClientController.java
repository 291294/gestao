package com.erp.moveis.controller;

import com.erp.moveis.model.Client;
import com.erp.moveis.service.ClientService;
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
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
@Tag(name = "Clientes", description = "Gestão de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    @Autowired
    private ClientService service;

    @GetMapping
    @PreAuthorize("hasAuthority('client.list')")
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<List<Client>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('client.view')")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<Optional<Client>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('client.create')")
    @Operation(summary = "Criar novo cliente")
    public ResponseEntity<Client> create(@RequestBody Client client) {
        return ResponseEntity.ok(service.save(client));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client.update')")
    @Operation(summary = "Atualizar cliente existente")
    public ResponseEntity<Client> update(@PathVariable Long id, @RequestBody Client client) {
        return ResponseEntity.ok(service.update(id, client));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('client.delete')")
    @Operation(summary = "Deletar cliente")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}