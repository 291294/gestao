package com.erp.moveis.serviceassistance.controller;

import com.erp.moveis.serviceassistance.dto.ServiceAssistanceRequest;
import com.erp.moveis.serviceassistance.dto.ServiceAssistanceResponse;
import com.erp.moveis.serviceassistance.service.ServiceAssistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/service-assistances")
@Tag(name = "Assistência de Serviço", description = "Gestão de assistências técnicas")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class ServiceAssistanceController {

    private final ServiceAssistanceService service;

    private static final Path UPLOAD_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "erp-service-photos");

    @GetMapping
    @Operation(summary = "Listar todas as assistências")
    public ResponseEntity<List<ServiceAssistanceResponse>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar assistência por ID")
    public ResponseEntity<ServiceAssistanceResponse> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Criar nova assistência")
    public ResponseEntity<ServiceAssistanceResponse> create(@Valid @RequestBody ServiceAssistanceRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar assistência")
    public ResponseEntity<ServiceAssistanceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ServiceAssistanceRequest request) {
        return service.update(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar assistência")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping(value = "/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de foto da assistência")
    public ResponseEntity<ServiceAssistanceResponse> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        Files.createDirectories(UPLOAD_DIR);
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename()
                .replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destination = UPLOAD_DIR.resolve(filename);
        file.transferTo(destination);

        String photoUrl = "/api/service-assistances/photos/" + filename;

        ServiceAssistanceRequest updateRequest = service.findById(id)
                .map(existing -> {
                    ServiceAssistanceRequest r = new ServiceAssistanceRequest();
                    r.setClientName(existing.getClientName());
                    r.setClientAddress(existing.getClientAddress());
                    r.setScheduledDate(existing.getScheduledDate());
                    r.setServiceDescription(existing.getServiceDescription());
                    r.setMaterialRequestedAt(existing.getMaterialRequestedAt());
                    r.setPhotoUrl(photoUrl);
                    r.setNotes(existing.getNotes());
                    r.setStatus(existing.getStatus());
                    return r;
                }).orElse(null);

        if (updateRequest == null) {
            return ResponseEntity.notFound().build();
        }

        return service.update(id, updateRequest)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/photos/{filename:.+}")
    @Operation(summary = "Servir foto da assistência")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String filename) throws IOException {
        Path filePath = UPLOAD_DIR.resolve(filename).normalize();
        if (!filePath.startsWith(UPLOAD_DIR) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/jpeg"))
                .body(bytes);
    }
}
