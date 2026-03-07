package com.erp.moveis.sales.dto;

import com.erp.moveis.sales.entity.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private Long id;
    private String quoteNumber;
    private Long companyId;
    private String companyName;
    private Long clientId;
    private String clientName;
    private Long sellerId;
    private String sellerName;
    private QuoteStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal finalAmount;
    private LocalDate validUntil;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private List<QuoteItemResponse> items = new ArrayList<>();
}
