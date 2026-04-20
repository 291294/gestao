package com.erp.moveis.core.audit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit")
@Tag(name = "Auditoria", description = "Logs de auditoria do sistema")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAuthority('audit.list')")
    @Operation(summary = "Listar logs por empresa")
    public ResponseEntity<List<AuditLog>> listByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(auditLogRepository.findByCompanyIdOrderByCreatedAtDesc(companyId));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('audit.view')")
    @Operation(summary = "Listar logs por entidade")
    public ResponseEntity<List<AuditLog>> listByEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId));
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAuthority('audit.list')")
    @Operation(summary = "Listar logs por usuario")
    public ResponseEntity<List<AuditLog>> listByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditLogRepository.findByUsernameOrderByCreatedAtDesc(username));
    }
}
