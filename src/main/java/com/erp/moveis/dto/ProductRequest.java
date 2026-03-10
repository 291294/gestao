package com.erp.moveis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    private String material;
    private String color;
    @Positive(message = "Preço deve ser positivo")
    private Double basePrice;
}
