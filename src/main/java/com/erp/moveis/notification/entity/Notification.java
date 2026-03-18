package com.erp.moveis.notification.entity;

import com.erp.moveis.core.tenant.TenantAware;
import com.erp.moveis.core.tenant.TenantEntityListener;
import com.erp.moveis.notification.type.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "notifications")
@EntityListeners(TenantEntityListener.class)
@Filter(name = "tenantFilter", condition = "company_id = :companyId")
public class Notification implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "read", nullable = false)
    private Boolean read = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
