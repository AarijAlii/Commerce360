package Commerce360.controller;

import Commerce360.dto.CreatePurchaseOrderRequest;
import Commerce360.dto.PurchaseOrderDTO;
import Commerce360.entity.PurchaseOrderStatus;
import Commerce360.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<PurchaseOrderDTO> submitPurchaseOrder(@PathVariable UUID id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.submitPurchaseOrder(id);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<PurchaseOrderDTO> approvePurchaseOrder(@PathVariable UUID id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.approvePurchaseOrder(id);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<PurchaseOrderDTO> rejectPurchaseOrder(@PathVariable UUID id, @RequestParam String reason) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.rejectPurchaseOrder(id, reason);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<PurchaseOrderDTO> markAsShipped(@PathVariable UUID id, @RequestParam String trackingNumber) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.markAsShipped(id, trackingNumber);
        return ResponseEntity.ok(purchaseOrder);
    }

    @PutMapping("/{id}/receive")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    public ResponseEntity<PurchaseOrderDTO> receiveShipment(@PathVariable UUID id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.receiveShipment(id);
        return ResponseEntity.ok(purchaseOrder);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PurchaseOrderDTO>> getPurchaseOrdersByStore(
            @PathVariable UUID storeId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PurchaseOrderDTO> orders = purchaseOrderService.getPurchaseOrdersByStore(storeId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/supplier/{supplierId}")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
    public ResponseEntity<Page<PurchaseOrderDTO>> getPurchaseOrdersBySupplier(
            @PathVariable UUID supplierId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PurchaseOrderDTO> orders = purchaseOrderService.getPurchaseOrdersBySupplier(supplierId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('SUPPLIER') or hasRole('ADMIN')")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrder(@PathVariable UUID id) {
        PurchaseOrderDTO purchaseOrder = purchaseOrderService.getPurchaseOrder(id);
        return ResponseEntity.ok(purchaseOrder);
    }
}
