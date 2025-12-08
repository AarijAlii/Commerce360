package Commerce360.entity;

public enum TransactionType {
    STOCK_IN, // General stock intake
    SALE, // General sale
    CUSTOMER_SALE, // Sale to customer (B2C)
    SUPPLIER_PURCHASE, // Purchase from supplier (B2B)
    EXPIRED, // Stock expired
    DAMAGED, // Stock damaged
    ADJUSTMENT, // Manual adjustment
    REMOVAL // Stock removal
}