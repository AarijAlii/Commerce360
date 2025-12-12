package Commerce360.service;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Commerce360.dto.InventoryDTO;
import Commerce360.entity.*;
import Commerce360.entity.TransactionType;
import Commerce360.repository.*;
import Commerce360.security.SecurityContextUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class InventoryService {

        @Autowired
        private final InventoryRepository inventoryRepository;

        @Autowired
        private final StoreRepository storeRepository;

        @Autowired
        private final ProductRepository productRepository;

        @Autowired
        private final TransactionRepository transactionRepository;

        @Autowired
        private final AuditLogService auditLogService;

        @Autowired
        private final SecurityContextUtil securityContextUtil;

        public InventoryService(
                        InventoryRepository inventoryRepository,
                        StoreRepository storeRepository,
                        ProductRepository productRepository,
                        TransactionRepository transactionRepository,
                        AuditLogService auditLogService,
                        SecurityContextUtil securityContextUtil) {
                this.inventoryRepository = inventoryRepository;
                this.storeRepository = storeRepository;
                this.productRepository = productRepository;
                this.transactionRepository = transactionRepository;
                this.auditLogService = auditLogService;
                this.securityContextUtil = securityContextUtil;
        }

        @Transactional
        // @CacheEvict(value = { "inventory", "inventory_summary" }, key = "#storeId")
        public InventoryDTO stockIn(UUID storeId, UUID productId, Integer quantity,
                        LocalDateTime expiryDate, String batchNumber, String notes) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                // Check if user is store owner or admin
                UUID currentUserId = securityContextUtil.getCurrentUserId();
                if (!store.getOwner().getId().equals(currentUserId) &&
                                !securityContextUtil.getCurrentUser().get().getRole().equals(UserRole.ADMIN)) {
                        throw new RuntimeException("You can only manage inventory for your own stores");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                // Use product price from catalog
                Double unitPrice = product.getPrice().doubleValue();

                Inventory inventory;

                // If expiry date is provided, create a new inventory entry
                if (expiryDate != null) {
                        inventory = Inventory.builder()
                                        .store(store)
                                        .product(product)
                                        .quantity(quantity)
                                        .unitPrice(unitPrice)
                                        .lastUpdated(LocalDateTime.now())
                                        .expiryDate(expiryDate)
                                        .batchNumber(batchNumber)
                                        .notes(notes)
                                        .build();
                } else {
                        // If no expiry date, try to find existing inventory without expiry date
                        inventory = inventoryRepository.findByStoreAndProduct(store, product)
                                        .orElse(Inventory.builder()
                                                        .store(store)
                                                        .product(product)
                                                        .quantity(0)
                                                        .unitPrice(unitPrice)
                                                        .lastUpdated(LocalDateTime.now())
                                                        .build());

                        // Update existing inventory
                        inventory.setQuantity(inventory.getQuantity() + quantity);
                        inventory.setUnitPrice(unitPrice);
                        inventory.setLastUpdated(LocalDateTime.now());
                        inventory.setBatchNumber(batchNumber);
                        inventory.setNotes(notes);
                }

                inventory = inventoryRepository.save(inventory);

                Transaction transaction = Transaction.builder()
                                .store(store)
                                .product(product)
                                .type(TransactionType.STOCK_IN)
                                .quantity(quantity)
                                .unitPrice(unitPrice)
                                .totalAmount(quantity * unitPrice)
                                .transactionDate(LocalDateTime.now())
                                .expiryDate(expiryDate)
                                .batchNumber(batchNumber)
                                .notes(notes)
                                .supplier(product.getSupplier()) // Set supplier from product
                                .build();

                transactionRepository.save(transaction);

                auditLogService.logAction("STOCK_IN", "INVENTORY", inventory.getId(),
                                String.format("Stocked in %d units of %s at $%.2f per unit", quantity,
                                                product.getName(), unitPrice));

                return InventoryDTO.fromEntity(inventory);
        }

        @Transactional
        // @CacheEvict(value = { "inventory", "inventory_summary" }, key = "#storeId")
        public InventoryDTO recordSale(UUID storeId, UUID productId, Integer quantity, String notes) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                // Check if user is store owner or admin
                UUID currentUserId = securityContextUtil.getCurrentUserId();
                if (!store.getOwner().getId().equals(currentUserId) &&
                                !securityContextUtil.getCurrentUser().get().getRole().equals(UserRole.ADMIN)) {
                        throw new RuntimeException("You can only manage inventory for your own stores");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByStoreAndProduct(store, product)
                                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

                if (inventory.getQuantity() < quantity) {
                        throw new RuntimeException("Insufficient stock");
                }

                // Use product price from catalog
                Double unitPrice = product.getPrice().doubleValue();

                inventory.setQuantity(inventory.getQuantity() - quantity);
                inventory.setLastUpdated(LocalDateTime.now());
                inventory = inventoryRepository.save(inventory);

                Transaction transaction = Transaction.builder()
                                .store(store)
                                .product(product)
                                .type(TransactionType.SALE)
                                .quantity(quantity)
                                .unitPrice(unitPrice)
                                .totalAmount(quantity * unitPrice)
                                .transactionDate(LocalDateTime.now())
                                .notes(notes)
                                .supplier(product.getSupplier()) // Set supplier from product
                                .build();

                transactionRepository.save(transaction);

                auditLogService.logAction("SALE", "INVENTORY", inventory.getId(),
                                String.format("Sold %d units of %s at $%.2f per unit", quantity, product.getName(),
                                                unitPrice));

                return InventoryDTO.fromEntity(inventory);
        }

        @Transactional
        // @CacheEvict(value = { "inventory", "inventory_summary" }, key = "#storeId")
        public InventoryDTO removeStock(UUID storeId, UUID productId, Integer quantity, String reason) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                // Check if user is store owner or admin
                UUID currentUserId = securityContextUtil.getCurrentUserId();
                if (!store.getOwner().getId().equals(currentUserId) &&
                                !securityContextUtil.getCurrentUser().get().getRole().equals(UserRole.ADMIN)) {
                        throw new RuntimeException("You can only manage inventory for your own stores");
                }

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Product not found"));

                Inventory inventory = inventoryRepository.findByStoreAndProduct(store, product)
                                .orElseThrow(() -> new RuntimeException("Product not found in inventory"));

                if (inventory.getQuantity() < quantity) {
                        throw new RuntimeException("Insufficient stock");
                }

                inventory.setQuantity(inventory.getQuantity() - quantity);
                inventory.setLastUpdated(LocalDateTime.now());
                inventory = inventoryRepository.save(inventory);

                Transaction transaction = Transaction.builder()
                                .store(store)
                                .product(product)
                                .type(TransactionType.REMOVAL)
                                .quantity(quantity)
                                .unitPrice(inventory.getUnitPrice())
                                .totalAmount(quantity * inventory.getUnitPrice())
                                .transactionDate(LocalDateTime.now())
                                .notes(reason)
                                .supplier(product.getSupplier()) // Set supplier from product
                                .build();

                transactionRepository.save(transaction);

                auditLogService.logAction("REMOVE_STOCK", "INVENTORY", inventory.getId(),
                                String.format("Removed %d units of %s. Reason: %s", quantity, product.getName(),
                                                reason));

                return InventoryDTO.fromEntity(inventory);
        }

        // @Cacheable(value = "inventory", key = "#storeId + '_' + #pageable.pageNumber
        // + '_' + #pageable.pageSize")
        public Page<InventoryDTO> getStoreInventory(UUID storeId, Pageable pageable) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                // Check if user is store owner or admin
                UUID currentUserId = securityContextUtil.getCurrentUserId();
                User currentUser = securityContextUtil.getCurrentUser().get();

                boolean isOwner = store.getOwner().getUser().getId().equals(currentUserId);
                boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);

                if (!isOwner && !isAdmin) {
                        throw new RuntimeException("You can only view inventory for your own stores");
                }

                return inventoryRepository.findAll(
                                (Specification<Inventory>) (root, query, cb) -> cb.equal(root.get("store"), store),
                                pageable)
                                .map(InventoryDTO::fromEntity);
        }

        // @Cacheable(value = "inventory", key = "#storeId + '_expiring_' + #before +
        // '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public Page<InventoryDTO> getExpiringStock(UUID storeId, LocalDateTime before, Pageable pageable) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                // Check if user is store owner or admin
                UUID currentUserId = securityContextUtil.getCurrentUserId();
                User currentUser = securityContextUtil.getCurrentUser().get();

                boolean isOwner = store.getOwner().getUser().getId().equals(currentUserId);
                boolean isAdmin = currentUser.getRole().equals(UserRole.ADMIN);

                if (!isOwner && !isAdmin) {
                        throw new RuntimeException("You can only view expiring inventory for your own stores");
                }

                return inventoryRepository.findAll(
                                (Specification<Inventory>) (root, query, cb) -> cb.and(
                                                cb.equal(root.get("store"), store),
                                                cb.lessThan(root.get("expiryDate"), before)),
                                pageable)
                                .map(InventoryDTO::fromEntity);
        }

        // @Cacheable(value = "inventory", key = "#storeId + '_category_' + #categoryId
        // + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
        public Page<InventoryDTO> getStoreInventoryByCategory(UUID storeId, UUID categoryId, Pageable pageable) {
                Store store = storeRepository.findById(storeId)
                                .orElseThrow(() -> new RuntimeException("Store not found"));

                if (!store.getOwner().getId().equals(securityContextUtil.getCurrentUserId())) {
                        throw new RuntimeException("You can only view inventory for your own stores");
                }

                return inventoryRepository.findAll(
                                (Specification<Inventory>) (root, query, cb) -> cb.and(
                                                cb.equal(root.get("store"), store),
                                                cb.equal(root.get("product").get("category").get("id"), categoryId)),
                                pageable)
                                .map(InventoryDTO::fromEntity);
        }

        @Transactional
        public void removeExpiredStock() {
                LocalDateTime now = LocalDateTime.now();
                List<Inventory> expiredInventory = inventoryRepository.findByExpiryDateBefore(now);

                for (Inventory inventory : expiredInventory) {
                        // Create a transaction record for the expired stock
                        Transaction transaction = Transaction.builder()
                                        .store(inventory.getStore())
                                        .product(inventory.getProduct())
                                        .type(TransactionType.EXPIRED)
                                        .quantity(inventory.getQuantity())
                                        .unitPrice(inventory.getUnitPrice())
                                        .totalAmount(inventory.getQuantity() * inventory.getUnitPrice())
                                        .transactionDate(now)
                                        .expiryDate(inventory.getExpiryDate())
                                        .batchNumber(inventory.getBatchNumber())
                                        .notes("Stock expired")
                                        .supplier(inventory.getProduct().getSupplier()) // Set supplier from product
                                        .build();

                        transactionRepository.save(transaction);

                        // Delete the expired inventory
                        inventoryRepository.delete(inventory);

                        auditLogService.logAction("REMOVE_EXPIRED", "INVENTORY", inventory.getId(),
                                        String.format("Automatically removed %d expired units of %s",
                                                        inventory.getQuantity(), inventory.getProduct().getName()));
                }
        }
}
