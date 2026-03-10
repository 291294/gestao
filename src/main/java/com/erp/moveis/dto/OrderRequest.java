package com.erp.moveis.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    @NotNull(message = "companyId é obrigatório")
    private Long companyId;
    @NotNull(message = "clientId é obrigatório")
    private Long clientId;
    private String status;
    @Valid
    private List<OrderItemRequest> items;
}
