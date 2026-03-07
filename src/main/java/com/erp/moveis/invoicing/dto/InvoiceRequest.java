package com.erp.moveis.invoicing.dto;

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
public class InvoiceRequest {
    private Long companyId;
    private Long clientId;
    private Long orderId;
    private Long deliveryId;
    private LocalDate dueDate;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private String notes;
    private List<InvoiceItemRequest> items = new ArrayList<>();
}
