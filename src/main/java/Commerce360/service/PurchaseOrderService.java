package Commerce360.service;

import Commerce360.dto.CreatePurchaseOrderRequest;
import Commerce360.dto.PurchaseOrderDTO;
import Commerce360.entity.*;
import Commerce360.repository.*;
import Commerce360.security.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class PurchaseOrderService {

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private SupplierProductRepository supplierProductRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SupplierProductService supplierProductService;

    @Autowired
    private SecurityContextUtil securityContextUtil;

    @Transactional
    public PurchaseOrderDTO createPurchaseOrder(CreatePurchaseOrderRequest request) {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        // Validate supplier is approved
        if (supplier.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new RuntimeException("Supplier is not approved");
        }

        // Generate order number
        String orderNumber = "PO-" + System.currentTimeMillis();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .orderNumber(orderNumber)
                .store(store)
                .supplier(supplier)
                .orderDate(LocalDateTime.now())
                .expectedDeliveryDate(request.getExpectedDeliveryDate())
                .status(PurchaseOrderStatus.DRAFT)
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .purchaseOrderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Add items
        for (CreatePurchaseOrderRequest.PurchaseOrderItemRequest itemRequest : request.getItems()) {
            SupplierProduct supplierProduct = supplierProductRepository.findById(itemRequest.getSupplierProductId())
                    .orElseThrow(() -> new RuntimeException("Supplier product not found"));

            // Validate supplier matches
            if (!supplierProduct.getSupplier().getId().equals(supplier.getId())) {
                throw new RuntimeException("Supplier product does not belong to selected supplier");
            }

            // Validate minimum order quantity
            if (itemRequest.getQuantity() < supplierProduct.getMinimumOrderQuantity()) {
                throw new RuntimeException("Quantity is less than minimum order quantity for " +
                        supplierProduct.getProduct().getName());
            }

            // Validate stock availability
            if (supplierProduct.getStockAvailable() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + supplierProduct.getProduct().getName());
            }

            BigDecimal itemTotal = supplierProduct.getSupplierPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .purchaseOrder(purchaseOrder)
                    .supplierProduct(supplierProduct)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(supplierProduct.getSupplierPrice())
                    .totalPrice(itemTotal)
                    .receivedQuantity(0)
                    .productName(supplierProduct.getProduct().getName())
                    .productSku(supplierProduct.getProduct().getSku())
                    .build();

            purchaseOrder.getPurchaseOrderItems().add(item);
        }

        purchaseOrder.setTotalAmount(totalAmount);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null), 
                store,
                "CREATE_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Created purchase order " + orderNumber + " for supplier " + supplier.getCompanyName());

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDTO submitPurchaseOrder(UUID purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Only draft orders can be submitted");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.SUBMITTED);
        purchaseOrder.setSubmittedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                purchaseOrder.getStore(),
                "SUBMIT_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Submitted purchase order " + purchaseOrder.getOrderNumber());

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDTO approvePurchaseOrder(UUID purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted orders can be approved");
        }

        // Reserve stock from supplier
        for (PurchaseOrderItem item : purchaseOrder.getPurchaseOrderItems()) {
            supplierProductService.updateStock(item.getSupplierProduct().getId(), -item.getQuantity());
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovedAt(LocalDateTime.now());
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                purchaseOrder.getStore(),
                "APPROVE_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Approved purchase order " + purchaseOrder.getOrderNumber());

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDTO rejectPurchaseOrder(UUID purchaseOrderId, String reason) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SUBMITTED) {
            throw new RuntimeException("Only submitted orders can be rejected");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);
        purchaseOrder.setRejectedAt(LocalDateTime.now());
        purchaseOrder.setRejectionReason(reason);
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                purchaseOrder.getStore(),
                "REJECT_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Rejected purchase order " + purchaseOrder.getOrderNumber() + ": " + reason);

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDTO markAsShipped(UUID purchaseOrderId, String trackingNumber) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED &&
                purchaseOrder.getStatus() != PurchaseOrderStatus.PROCESSING) {
            throw new RuntimeException("Only approved or processing orders can be shipped");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.SHIPPED);
        purchaseOrder.setShippedAt(LocalDateTime.now());
        purchaseOrder.setTrackingNumber(trackingNumber);
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                purchaseOrder.getStore(),
                "SHIP_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Shipped purchase order " + purchaseOrder.getOrderNumber() + " with tracking: " + trackingNumber);

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    @Transactional
    public PurchaseOrderDTO receiveShipment(UUID purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SHIPPED) {
            throw new RuntimeException("Only shipped orders can be received");
        }

        // Update inventory for each item
        for (PurchaseOrderItem item : purchaseOrder.getPurchaseOrderItems()) {
            // Find or create inventory record
            Inventory inventory = inventoryRepository
                    .findByStoreAndProduct(purchaseOrder.getStore(), item.getSupplierProduct().getProduct())
                    .orElse(Inventory.builder()
                            .store(purchaseOrder.getStore())
                            .product(item.getSupplierProduct().getProduct())
                            .quantity(0)
                            .reservedQuantity(0)
                            .unitPrice(item.getUnitPrice().doubleValue())
                            .lastUpdated(LocalDateTime.now())
                            .build());

            inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
            inventory.setUnitPrice(item.getUnitPrice().doubleValue());
            inventory.setLastUpdated(LocalDateTime.now());
            inventory.setPurchaseOrder(purchaseOrder);
            inventoryRepository.save(inventory);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                    .store(purchaseOrder.getStore())
                    .product(item.getSupplierProduct().getProduct())
                    .type(TransactionType.SUPPLIER_PURCHASE)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice().doubleValue())
                    .totalAmount(item.getTotalPrice().doubleValue())
                    .transactionDate(LocalDateTime.now())
                    .supplier(purchaseOrder.getSupplier())
                    .referenceNumber(purchaseOrder.getOrderNumber())
                    .orderId(purchaseOrder.getId())
                    .orderType(OrderType.PURCHASE_ORDER)
                    .notes("Received from purchase order " + purchaseOrder.getOrderNumber())
                    .build();
            transactionRepository.save(transaction);

            // Update received quantity
            item.setReceivedQuantity(item.getQuantity());
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.DELIVERED);
        purchaseOrder.setDeliveredAt(LocalDateTime.now());
        purchaseOrder.setUpdatedAt(LocalDateTime.now());
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        // Update supplier stats
        Supplier supplier = purchaseOrder.getSupplier();
        supplier.setTotalOrders(supplier.getTotalOrders() + 1);
        supplierRepository.save(supplier);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                purchaseOrder.getStore(),
                "RECEIVE_PURCHASE_ORDER",
                "PurchaseOrder",
                purchaseOrder.getId(),
                "Received shipment for purchase order " + purchaseOrder.getOrderNumber());

        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }

    public Page<PurchaseOrderDTO> getPurchaseOrdersByStore(UUID storeId, PurchaseOrderStatus status,
            Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Page<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseOrderRepository.findByStoreAndStatus(store, status, pageable);
        } else {
            orders = purchaseOrderRepository.findByStore(store, pageable);
        }

        return orders.map(PurchaseOrderDTO::fromEntity);
    }

    public Page<PurchaseOrderDTO> getPurchaseOrdersBySupplier(UUID supplierId, PurchaseOrderStatus status,
            Pageable pageable) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        Page<PurchaseOrder> orders;
        if (status != null) {
            orders = purchaseOrderRepository.findBySupplierAndStatus(supplier, status, pageable);
        } else {
            orders = purchaseOrderRepository.findBySupplier(supplier, pageable);
        }

        return orders.map(PurchaseOrderDTO::fromEntity);
    }

    public PurchaseOrderDTO getPurchaseOrder(UUID purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));
        return PurchaseOrderDTO.fromEntity(purchaseOrder);
    }
}
