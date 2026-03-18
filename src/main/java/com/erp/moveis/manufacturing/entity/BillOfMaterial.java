package com.erp.moveis.manufacturing.entity;

import com.erp.moveis.core.tenant.TenantAware;
import com.erp.moveis.core.tenant.TenantEntityListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bill_of_materials")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
@Getter
@Setter
public class BillOfMaterial implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "bom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillOfMaterialItem> items = new ArrayList<>();
}
