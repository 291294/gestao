package com.erp.moveis.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryItemRequest {
    private Long productId;
    private Integer quantity;
    private String notes;
}
