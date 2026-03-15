package com.erp.moveis.controller;

import com.erp.moveis.dto.ClientRequest;
import com.erp.moveis.dto.ClientResponse;
import com.erp.moveis.dto.PageResponse;
import com.erp.moveis.core.export.ExportService;
import com.erp.moveis.mapper.ClientMapper;
import com.erp.moveis.model.Client;
import com.erp.moveis.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "*")
@Tag(name = "Clientes", description = "Gestão de clientes")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService service;
    private final ClientMapper mapper;
    private final ExportService exportService;

    @GetMapping
    @PreAuthorize("hasAuthority('client.list')")
    @Operation(summary = "Listar todos os clientes")
    public ResponseEntity<List<ClientResponse>> list() {
        return ResponseEntity.ok(service.list().stream().map(mapper::toResponse).toList());
    }

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('client.list')")
    @Operation(summary = "Listar clientes paginados")
    public ResponseEntity<PageResponse<ClientResponse>> listPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort) {
        var pageable = PageRequest.of(page, size, Sort.by(sort));
        var result = service.listPaged(pageable).map(mapper::toResponse);
        return ResponseEntity.ok(PageResponse.of(result));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('client.view')")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClientResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('client.create')")
    @Operation(summary = "Criar novo cliente")
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest request) {
        Client entity = mapper.toEntity(request);
        Client saved = service.save(entity);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('client.update')")
    @Operation(summary = "Atualizar cliente existente")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id, @Valid @RequestBody ClientRequest request) {
        return service.findById(id).map(existing -> {
            mapper.updateEntity(request, existing);
            Client saved = service.save(existing);
            return ResponseEntity.ok(mapper.toResponse(saved));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('client.delete')")
    @Operation(summary = "Deletar cliente")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('client.list')")
    @Operation(summary = "Exportar clientes em PDF")
    public void exportPdf(HttpServletResponse response) throws IOException {
        List<Client> clients = service.list();
        List<String> headers = List.of("ID", "Nome", "Telefone", "Email", "Profissão");
        List<List<String>> rows = clients.stream().map(c -> List.of(
                String.valueOf(c.getId()),
                c.getName() != null ? c.getName() : "",
                c.getPhone() != null ? c.getPhone() : "",
                c.getEmail() != null ? c.getEmail() : "",
                c.getProfession() != null ? c.getProfession() : ""
        )).toList();
        exportService.exportToPdf(response, "Relatório de Clientes", headers, rows, "clientes");
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('client.list')")
    @Operation(summary = "Exportar clientes em Excel")
    public void exportExcel(HttpServletResponse response) throws IOException {
        List<Client> clients = service.list();
        List<String> headers = List.of("ID", "Nome", "Telefone", "Email", "Profissão", "Preferências");
        List<List<String>> rows = clients.stream().map(c -> List.of(
                String.valueOf(c.getId()),
                c.getName() != null ? c.getName() : "",
                c.getPhone() != null ? c.getPhone() : "",
                c.getEmail() != null ? c.getEmail() : "",
                c.getProfession() != null ? c.getProfession() : "",
                c.getPreferences() != null ? c.getPreferences() : ""
        )).toList();
        exportService.exportToExcel(response, "Clientes", headers, rows, "clientes");
    }
}