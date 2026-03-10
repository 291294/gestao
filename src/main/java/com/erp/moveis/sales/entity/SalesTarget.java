package com.erp.moveis.sales.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_targets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "seller_id")
    private Long sellerId;

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
    private BigDecimal achievedAmount;

    @Column(name = "achievement_percentage", precision = 5, scale = 2)
    private BigDecimal achievementPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TargetStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TargetType {
        INDIVIDUAL,
        TEAM,
        COMPANY
    }

    public enum TargetStatus {
        ACTIVE,
        COMPLETED,
        ACHIEVED,
        EXPIRED,
        CANCELLED
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = TargetStatus.ACTIVE;
        if (this.achievedAmount == null) this.achievedAmount = BigDecimal.ZERO;
        if (this.achievementPercentage == null) this.achievementPercentage = BigDecimal.ZERO;
        calculateAchievement();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateAchievement();
    }

    public void calculateAchievement() {
        if (targetAmount != null && achievedAmount != null && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.achievementPercentage = achievedAmount
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, java.math.RoundingMode.HALF_UP);
        }
    }
}
