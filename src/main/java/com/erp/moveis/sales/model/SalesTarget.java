package com.erp.moveis.sales.model;

import com.erp.moveis.core.company.model.Company;
import com.erp.moveis.core.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "target_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "achieved_amount", precision = 15, scale = 2)
    private BigDecimal achievedAmount = BigDecimal.ZERO;

    @Column(name = "achievement_percentage", precision = 5, scale = 2)
    private BigDecimal achievementPercentage = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetStatus status = TargetStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TargetType {
        INDIVIDUAL,  // Meta individual de vendedor
        TEAM,        // Meta da equipe
        COMPANY      // Meta da empresa
    }

    public enum TargetStatus {
        ACTIVE,      // Ativa
        COMPLETED,   // Completa
        CANCELLED    // Cancelada
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper method to calculate achievement percentage
    public void calculateAchievement() {
        if (targetAmount != null && achievedAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.achievementPercentage = achievedAmount
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, java.math.RoundingMode.HALF_UP);
        }
    }

    @PrePersist
    @PreUpdate
    protected void calculateBeforeSave() {
        calculateAchievement();
    }
}
