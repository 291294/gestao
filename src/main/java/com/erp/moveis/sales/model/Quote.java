package com.erp.moveis.sales.model;

import com.erp.moveis.core.company.entity.Company;
import com.erp.moveis.core.user.entity.User;
import com.erp.moveis.model.Client;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "quote_number", nullable = false, unique = true, length = 50)
    private String quoteNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuoteStatus status = QuoteStatus.DRAFT;

    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 15, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 15, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuoteItem> items = new ArrayList<>();

    public enum QuoteStatus {
        DRAFT,       // Rascunho
        SENT,        // Enviado ao cliente
        APPROVED,    // Aprovado pelo cliente
        REJECTED,    // Rejeitado
        EXPIRED,     // Expirado (passou da validade)
        CONVERTED    // Convertido em pedido
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods
    public void addItem(QuoteItem item) {
        items.add(item);
        item.setQuote(this);
    }

    public void removeItem(QuoteItem item) {
        items.remove(item);
        item.setQuote(null);
    }

    public void calculateTotals() {
        this.totalAmount = items.stream()
                .map(QuoteItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.finalAmount = this.totalAmount
                .subtract(this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO)
                .add(this.taxAmount != null ? this.taxAmount : BigDecimal.ZERO);
    }
}
