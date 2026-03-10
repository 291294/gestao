package com.erp.moveis.core.scheduler;

import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryScheduler {

    private final InventoryItemRepository repository;

    @Scheduled(cron = "0 0 6 * * *")
    public void checkLowStock() {
        List<InventoryItem> lowStock = repository.findAllLowStock();

        if (lowStock.isEmpty()) {
            return;
        }

        for (InventoryItem item : lowStock) {
            if (item.getQuantityOnHand() == 0) {
                log.error("[STOCK_OUT] Product {} (company {}) is OUT OF STOCK",
                        item.getProductId(), item.getCompanyId());
            } else {
                log.warn("[STOCK_LOW] Product {} (company {}) has only {} units (min: {})",
                        item.getProductId(), item.getCompanyId(),
                        item.getQuantityOnHand(), item.getMinStockLevel());
            }
        }

        log.info("Low stock check completed: {} items flagged", lowStock.size());
    }
}
