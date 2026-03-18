package com.erp.moveis.model;

import com.erp.moveis.core.tenant.TenantAware;
import com.erp.moveis.core.tenant.TenantEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import java.io.Serializable;

@Entity
@Table(name = "products")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
public class Product implements Serializable, TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    private String material;

    private String color;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "created_at")
    private Long createdAt;

    public Product() {
    }

    public Product(String name, String material, String color, Double basePrice) {
        this.name = name;
        this.material = material;
        this.color = color;
        this.basePrice = basePrice;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }
}