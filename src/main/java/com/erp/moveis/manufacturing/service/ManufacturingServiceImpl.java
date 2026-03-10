package com.erp.moveis.manufacturing.service;

import com.erp.moveis.core.exception.BusinessException;
import com.erp.moveis.core.exception.ResourceNotFoundException;
import com.erp.moveis.inventory.service.InventoryService;
import com.erp.moveis.manufacturing.entity.*;
import com.erp.moveis.manufacturing.repository.BillOfMaterialRepository;
import com.erp.moveis.manufacturing.repository.ProductionOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManufacturingServiceImpl implements ManufacturingService {

    private static final Long DEFAULT_WAREHOUSE_ID = 1L;

    private final ProductionOrderRepository orderRepository;
    private final BillOfMaterialRepository bomRepository;
    private final InventoryService inventoryService;

    @Override
    @Transactional
    public ProductionOrder createProductionOrder(Long companyId, Long productId, BigDecimal quantity) {
        ProductionOrder order = new ProductionOrder();
        order.setCompanyId(companyId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setStatus(ProductionStatus.CREATED);
        return orderRepository.save(order);
    }

    @Override
    public List<ProductionOrder> findByCompany(Long companyId) {
        if (companyId == null) {
            return orderRepository.findAll();
        }
        return orderRepository.findByCompanyId(companyId);
    }

    @Override
    @Transactional
    public void startProduction(Long orderId) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Production order not found with id: " + orderId));

        if (order.getStatus() != ProductionStatus.CREATED) {
            throw new BusinessException("Production order must be in CREATED status to start");
        }

        BillOfMaterial bom = bomRepository.findByProductId(order.getProductId());
        if (bom == null || bom.getItems() == null || bom.getItems().isEmpty()) {
            throw new BusinessException("No Bill of Materials found for product: " + order.getProductId());
        }

        for (BillOfMaterialItem item : bom.getItems()) {
            BigDecimal totalNeeded = item.getQuantity().multiply(order.getQuantity());

            inventoryService.removeWarehouseStock(
                    order.getCompanyId(),
                    item.getMaterialProductId(),
                    DEFAULT_WAREHOUSE_ID,
                    totalNeeded
            );
        }

        order.setStatus(ProductionStatus.IN_PROGRESS);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void finishProduction(Long orderId) {
        ProductionOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Production order not found with id: " + orderId));

        if (order.getStatus() != ProductionStatus.IN_PROGRESS) {
            throw new BusinessException("Production order must be IN_PROGRESS to finish");
        }

        inventoryService.addWarehouseStock(
                order.getCompanyId(),
                order.getProductId(),
                DEFAULT_WAREHOUSE_ID,
                order.getQuantity()
        );

        order.setStatus(ProductionStatus.FINISHED);
        orderRepository.save(order);
    }
}
