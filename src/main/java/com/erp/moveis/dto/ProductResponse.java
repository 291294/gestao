package com.erp.moveis.dto;

import lombok.Data;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String material;
    private String color;
    private Double basePrice;
    private Long createdAt;
}
