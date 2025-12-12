package Commerce360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import Commerce360.dto.InventoryDTO;
import Commerce360.service.InventoryService;
import Commerce360.service.ReportService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.Data;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Management", description = "Store inventory tracking, stock movements, and expiry management")
public class InventoryController {

    @Autowired
    private final InventoryService inventoryService;

    @Autowired
    private final ReportService reportService;

    public InventoryController(InventoryService inventoryService, ReportService reportService) {
        this.inventoryService = inventoryService;
        this.reportService = reportService;
    }

    @PostMapping("/stock-in")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Add Stock (Stock In)", description = "Add inventory to store. Used after receiving supplier deliveries or manual restocking (STORE_MANAGER only)")
    public ResponseEntity<InventoryDTO> stockIn(
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestBody(required = false) StockInRequest request) {

        LocalDateTime expiryDate = request != null ? request.getExpiryDate() : null;
        String batchNumber = request != null ? request.getBatchNumber() : null;
        String notes = request != null ? request.getNotes() : null;

        return ResponseEntity
                .ok(inventoryService.stockIn(storeId, productId, quantity, expiryDate, batchNumber, notes));
    }

    @PostMapping("/sale")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Record Sale (Stock Out)", description = "Reduce inventory after a sale to customer. Creates transaction record (STORE_MANAGER only)")
    public ResponseEntity<InventoryDTO> recordSale(
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestBody(required = false) SaleRequest request) {

        String notes = request != null ? request.getNotes() : null;

        return ResponseEntity.ok(inventoryService.recordSale(storeId, productId, quantity, notes));
    }

    @PostMapping("/remove")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Remove Stock (Damage/Loss)", description = "Remove inventory due to damage, expiry, or loss. Requires reason (STORE_MANAGER only)")
    public ResponseEntity<InventoryDTO> removeStock(
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam Integer quantity,
            @RequestBody(required = false) RemoveStockRequest request) {

        String reason = request != null ? request.getReason() : "No reason provided";

        return ResponseEntity.ok(inventoryService.removeStock(storeId, productId, quantity, reason));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get Store Inventory", description = "Get all inventory items for a specific store with pagination (STORE_MANAGER only)")
    public ResponseEntity<Page<InventoryDTO>> getStoreInventory(
            @PathVariable UUID storeId,
            Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getStoreInventory(storeId, pageable));
    }

    @GetMapping("/store/{storeId}/category/{categoryId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get Inventory by Category", description = "Get store inventory filtered by product category (STORE_MANAGER only)")
    public ResponseEntity<Page<InventoryDTO>> getStoreInventoryByCategory(
            @PathVariable UUID storeId,
            @PathVariable UUID categoryId,
            Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getStoreInventoryByCategory(storeId, categoryId, pageable));
    }

    @GetMapping("/store/{storeId}/expiring")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Get Expiring Stock", description = "Get inventory items expiring before a specified date. Helps prevent waste (STORE_MANAGER only)")
    public ResponseEntity<Page<InventoryDTO>> getExpiringStock(
            @PathVariable UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getExpiringStock(storeId, before, pageable));
    }

    @GetMapping("/reports/stock")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Stock Movement Report", description = "Get stock movement summary for date range (stock in, stock out, current levels) (STORE_MANAGER only)")
    public ResponseEntity<Map<String, Object>> getStockSummary(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reportService.getStockSummary(storeId, startDate, endDate));
    }

    @GetMapping("/reports/sales")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Sales Report", description = "Get sales summary for date range (total sales, revenue, top products) (STORE_MANAGER only)")
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reportService.getSalesSummary(storeId, startDate, endDate));
    }

    @GetMapping("/reports/purchases")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Purchase Report", description = "Get purchase summary for date range (total purchases from suppliers) (STORE_MANAGER only)")
    public ResponseEntity<Map<String, Object>> getPurchaseSummary(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reportService.getPurchaseSummary(storeId, startDate, endDate));
    }

    @GetMapping("/reports/profit-loss")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(summary = "Profit & Loss Analysis", description = "Get profit/loss analysis for date range (revenue vs expenses, margins) (STORE_MANAGER only)")
    public ResponseEntity<Map<String, Object>> getProfitLossAnalysis(
            @RequestParam UUID storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(reportService.getProfitLossAnalysis(storeId, startDate, endDate));
    }
}

// Request classes
@Data
class StockInRequest {
    private LocalDateTime expiryDate;
    private String batchNumber;
    private String notes;
}

@Data
class SaleRequest {
    private String notes;
}

@Data
class RemoveStockRequest {
    private String reason;
}
