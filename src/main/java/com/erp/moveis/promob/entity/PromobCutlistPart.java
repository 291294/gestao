package com.erp.moveis.promob.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "promob_cutlist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromobCutlistPart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private PromobProject project;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "part_name", nullable = false)
    private String partName;

    private String material;

    @Column(precision = 10, scale = 2)
    private BigDecimal thickness;

    @Column(precision = 10, scale = 2)
    private BigDecimal width;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "edge_top")
    @Builder.Default
    private Boolean edgeTop = false;

    @Column(name = "edge_bottom")
    @Builder.Default
    private Boolean edgeBottom = false;

    @Column(name = "edge_left")
    @Builder.Default
    private Boolean edgeLeft = false;

    @Column(name = "edge_right")
    @Builder.Default
    private Boolean edgeRight = false;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.quantity == null) this.quantity = 1;
    }
}
