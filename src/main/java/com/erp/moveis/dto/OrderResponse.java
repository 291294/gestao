package com.erp.moveis.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long companyId;
    private Long clientId;
    private String clientName;
    private Double totalValue;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private List<OrderItemResponse> items;
}
