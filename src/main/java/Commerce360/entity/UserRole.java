package Commerce360.entity;

public enum UserRole {
    ADMIN,          // Platform owners who approve suppliers and store managers
    STORE_MANAGER,  // Kiryana store owners who sell to customers
    SUPPLIER,       // Large companies (LG, Nestle) that supply products to stores
    CUSTOMER        // End users who buy from stores
}
