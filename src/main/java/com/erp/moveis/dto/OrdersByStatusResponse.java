package com.erp.moveis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrdersByStatusResponse {
    private String status;
    private long count;
}
