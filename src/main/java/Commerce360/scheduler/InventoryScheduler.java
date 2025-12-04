package Commerce360.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import Commerce360.service.InventoryService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InventoryScheduler {

    @Autowired
    private final InventoryService inventoryService;

    public InventoryScheduler(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeExpiredStock() {
        log.info("Starting scheduled task to remove expired stock");
        try {
            inventoryService.removeExpiredStock();
            log.info("Successfully completed removing expired stock");
        } catch (Exception e) {
            log.error("Error removing expired stock: {}", e.getMessage(), e);
        }
    }
}