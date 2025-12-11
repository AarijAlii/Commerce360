package Commerce360.entity;

public enum OrderStatus {
    PENDING, // Order placed, awaiting confirmation
    CONFIRMED, // Store confirmed the order
    PROCESSING, // Store is preparing the order
    SHIPPED, // Order has been shipped
    DELIVERED, // Order delivered to customer
    CANCELLED, // Order cancelled by customer or store
    REFUNDED // Order refunded
}
