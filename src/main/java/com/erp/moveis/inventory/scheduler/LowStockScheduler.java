package com.erp.moveis.inventory.scheduler;

import com.erp.moveis.inventory.entity.InventoryItem;
import com.erp.moveis.inventory.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockScheduler {

    private final InventoryItemRepository inventoryItemRepository;

    /**
     * Verifica itens com estoque baixo ou zerado a cada 2 horas.
     * Loga alertas para auditoria e futura integração com notificações.
     */
    @Scheduled(cron = "0 0 */2 * * *")
    @Transactional(readOnly = true)
    public void checkLowStockAlerts() {
        List<InventoryItem> allItems = inventoryItemRepository.findAll();

        int lowCount = 0;
        int outCount = 0;

        for (InventoryItem item : allItems) {
            if (item.getQuantityOnHand() == 0) {
                outCount++;
                log.warn("[STOCK_OUT] Product {} in company {} has ZERO stock",
                        item.getProductId(), item.getCompanyId());
            } else if (item.isLowStock()) {
                lowCount++;
                log.warn("[STOCK_LOW] Product {} in company {} is below minimum. Available: {}, Min: {}",
                        item.getProductId(), item.getCompanyId(),
                        item.getQuantityAvailable(), item.getMinStockLevel());
            }
        }

        if (lowCount > 0 || outCount > 0) {
            log.info("[STOCK_ALERT] {} item(s) with low stock, {} item(s) out of stock", lowCount, outCount);
        }
    }
}
