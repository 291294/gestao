package com.erp.moveis.sales.model;

import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.model.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "commissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Commission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id")
    private Quote quote;

    @Column(name = "commission_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal commissionPercentage;

    @Column(name = "sale_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal saleAmount;

    @Column(name = "commission_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionStatus status = CommissionStatus.PENDING;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum CommissionStatus {
        PENDING,    // Pendente de aprovação
        APPROVED,   // Aprovada
        PAID,       // Paga
        CANCELLED   // Cancelada
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.commissionAmount == null) {
            calculateCommission();
        }
    }

    // Helper method to calculate commission
    public void calculateCommission() {
        if (saleAmount != null && commissionPercentage != null) {
            this.commissionAmount = saleAmount
                    .multiply(commissionPercentage)
                    .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    @PrePersist
    protected void calculateBeforeSave() {
        if (this.commissionAmount == null) {
            calculateCommission();
        }
    }
}
