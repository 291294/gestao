package com.erp.moveis.analytics.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopProductResponse {

    private Long productId;

    private Double totalSold;
}
