package com.erp.moveis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RevenueByMonthResponse {
    private String month;
    private double revenue;
}
