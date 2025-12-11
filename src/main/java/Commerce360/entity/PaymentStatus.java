package Commerce360.entity;

public enum PaymentStatus {
    PENDING, // Payment not yet processed
    PROCESSING, // Payment is being processed
    COMPLETED, // Payment successful
    FAILED, // Payment failed
    REFUNDED, // Payment refunded
    CANCELLED // Payment cancelled
}
