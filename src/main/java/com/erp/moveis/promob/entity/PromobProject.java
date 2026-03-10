package com.erp.moveis.promob.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promob_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromobProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "order_id")
    private Long orderId;

    private String environment;

    private String designer;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "imported_at", nullable = false)
    private LocalDateTime importedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromobProjectItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PromobCutlistPart> cutlistParts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.importedAt == null) this.importedAt = LocalDateTime.now();
        if (this.status == null) this.status = "IMPORTED";
    }
}
