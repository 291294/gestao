package com.erp.moveis.manufacturing.service;

import com.erp.moveis.manufacturing.entity.ProductionOrder;

import java.math.BigDecimal;

public interface ManufacturingService {

    ProductionOrder createProductionOrder(Long companyId, Long productId, BigDecimal quantity);

    void startProduction(Long orderId);

    void finishProduction(Long orderId);
}
