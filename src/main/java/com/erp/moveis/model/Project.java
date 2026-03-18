package com.erp.moveis.model;

import com.erp.moveis.core.tenant.TenantAware;
import com.erp.moveis.core.tenant.TenantEntityListener;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "projects")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
public class Project implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    private String description;

    private Double budget;

    @Column(name = "created_at")
    private Long createdAt;

    public Project() {
    }

    public Project(String name, Client client, String description, Double budget) {
        this.name = name;
        this.client = client;
        this.description = description;
        this.budget = budget;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
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