package com.erp.moveis.sales.dto;

import com.erp.moveis.sales.model.Quote.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequest {
    private Long companyId;
    private Long clientId;
    private Long sellerId;
    private LocalDate validUntil;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private String notes;
    private List<QuoteItemRequest> items = new ArrayList<>();
}
