package com.erp.moveis.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private String notes;
    private LocalDateTime createdAt;
}
