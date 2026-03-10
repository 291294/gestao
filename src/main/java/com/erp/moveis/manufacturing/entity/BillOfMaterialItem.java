package com.erp.moveis.manufacturing.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "bill_of_material_items")
@Getter
@Setter
public class BillOfMaterialItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_product_id", nullable = false)
    private Long materialProductId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial bom;
}
