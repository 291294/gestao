package com.erp.moveis.promob.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultResponse {

    private Long projectId;
    private String fileName;
    private String clientName;
    private Long clientId;
    private Long orderId;
    private int itemsImported;
    private int cutlistPartsImported;
    private int productionOrdersCreated;
    private BigDecimal totalValue;
    private String status;
    private String message;
}
