package com.erp.moveis.manufacturing.service;

import com.erp.moveis.manufacturing.entity.ProductionOrder;

import java.math.BigDecimal;
import java.util.List;

public interface ManufacturingService {

    ProductionOrder createProductionOrder(Long companyId, Long productId, BigDecimal quantity);

    List<ProductionOrder> findByCompany(Long companyId);

    void startProduction(Long orderId);

    void finishProduction(Long orderId);
}
