package Commerce360.entity;

public enum OrderType {
    CUSTOMER_ORDER, // Transaction from customer purchasing from store
    PURCHASE_ORDER, // Transaction from store purchasing from supplier
    MANUAL // Manual inventory adjustment
}
