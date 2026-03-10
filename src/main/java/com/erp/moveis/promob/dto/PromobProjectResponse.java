package com.erp.moveis.promob.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromobProjectResponse {

    private Long id;
    private Long companyId;
    private String fileName;
    private String clientName;
    private Long clientId;
    private Long orderId;
    private String environment;
    private String designer;
    private BigDecimal totalValue;
    private String status;
    private String notes;
    private LocalDateTime importedAt;
    private int itemCount;
    private int cutlistCount;
    private List<PromobItemResponse> items;
    private List<PromobCutlistResponse> cutlist;
}
