package Commerce360.entity;

public enum PurchaseOrderStatus {
    DRAFT, // Store manager is creating the order
    SUBMITTED, // Order submitted to supplier
    APPROVED, // Supplier approved the order
    REJECTED, // Supplier rejected the order
    PROCESSING, // Supplier is processing the order
    SHIPPED, // Supplier has shipped the order
    DELIVERED, // Store received the shipment
    CANCELLED // Order cancelled
}
