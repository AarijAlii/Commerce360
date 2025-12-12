package Commerce360.entity;

public enum PaymentStatus {
    PENDING, // Payment intent created, awaiting payment
    PROCESSING, // Payment is being processed
    REQUIRES_ACTION, // 3D Secure authentication required
    SUCCEEDED, // Payment completed successfully
    FAILED, // Payment failed
    CANCELED // Payment canceled
}
