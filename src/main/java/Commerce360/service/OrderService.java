package Commerce360.service;

import Commerce360.dto.OrderDTO;
import Commerce360.dto.StoreRatingDTO;
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
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StoreRatingRepository storeRatingRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CartService cartService;

    @Autowired
    private SecurityContextUtil securityContextUtil;

    @Transactional
    public OrderDTO placeOrder(UUID cartId, String shippingAddress, String shippingCity,
                              String shippingPostalCode, String contactPhone, String notes) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate and reserve inventory
        for (CartItem cartItem : cart.getCartItems()) {
            Inventory inventory = inventoryRepository
                    .findByStoreAndProduct(cart.getStore(), cartItem.getProduct())
                    .orElseThrow(() -> new RuntimeException("Product not available: " + cartItem.getProduct().getName()));

            if (inventory.getAvailableQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for: " + cartItem.getProduct().getName() +
                        ". Available: " + inventory.getAvailableQuantity());
            }
        }

        // Generate order number
        String orderNumber = "ORD-" + System.currentTimeMillis();

        // Create order
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customer(cart.getCustomer())
                .store(cart.getStore())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .shippingAddress(shippingAddress)
                .shippingCity(shippingCity)
                .shippingPostalCode(shippingPostalCode)
                .contactPhone(contactPhone)
                .notes(notes)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Create order items and reserve inventory
        for (CartItem cartItem : cart.getCartItems()) {
            BigDecimal itemTotal = cartItem.getUnitPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(cartItem.getUnitPrice())
                    .totalPrice(itemTotal)
                    .productName(cartItem.getProduct().getName())
                    .productSku(cartItem.getProduct().getSku())
                    .build();

            order.getOrderItems().add(orderItem);

            // Reserve inventory
            Inventory inventory = inventoryRepository
                    .findByStoreAndProduct(cart.getStore(), cartItem.getProduct())
                    .get();
            inventory.setReservedQuantity(inventory.getReservedQuantity() + cartItem.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        // Mark cart as converted
        cartService.markCartAsConverted(cartId);

        // Update customer order count
        customerService.incrementOrderCount(cart.getCustomer().getId());

        // Update store order count
        Store store = cart.getStore();
        store.setTotalOrders(store.getTotalOrders() + 1);
        storeRepository.save(store);

        // Audit log
        auditLogService.logAction(
                cart.getCustomer().getUser(),
                cart.getStore(),
                "PLACE_ORDER",
                "Order",
                order.getId(),
                "Customer placed order " + orderNumber + " for " + totalAmount + " PKR"
        );

        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO confirmOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be confirmed");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                order.getStore(),
                "CONFIRM_ORDER",
                "Order",
                order.getId(),
                "Order " + order.getOrderNumber() + " confirmed"
        );

        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO markAsProcessing(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Only confirmed orders can be marked as processing");
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO markAsShipped(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new RuntimeException("Only processing orders can be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO markAsDelivered(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new RuntimeException("Only shipped orders can be delivered");
        }

        // Update inventory - reduce actual stock and reserved quantity
        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = inventoryRepository
                    .findByStoreAndProduct(order.getStore(), item.getProduct())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventory.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inventory);

            // Create transaction record
            Transaction transaction = Transaction.builder()
                    .store(order.getStore())
                    .product(item.getProduct())
                    .type(TransactionType.CUSTOMER_SALE)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice().doubleValue())
                    .totalAmount(item.getTotalPrice().doubleValue())
                    .transactionDate(LocalDateTime.now())
                    .referenceNumber(order.getOrderNumber())
                    .orderId(order.getId())
                    .orderType(OrderType.CUSTOMER_ORDER)
                    .notes("Customer order delivered: " + order.getOrderNumber())
                    .build();
            transactionRepository.save(transaction);
        }

        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(PaymentStatus.COMPLETED); // Mark payment as complete
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Audit log
        auditLogService.logAction(
                securityContextUtil.getCurrentUser().orElse(null),
                order.getStore(),
                "DELIVER_ORDER",
                "Order",
                order.getId(),
                "Order " + order.getOrderNumber() + " delivered"
        );

        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO cancelOrder(UUID orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot cancel delivered or already cancelled orders");
        }

        // Release reserved inventory
        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = inventoryRepository
                    .findByStoreAndProduct(order.getStore(), item.getProduct())
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            inventory.setReservedQuantity(inventory.getReservedQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes(order.getNotes() != null ? order.getNotes() + "\nCancellation reason: " + reason : "Cancellation reason: " + reason);
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        return OrderDTO.fromEntity(order);
    }

    public Page<OrderDTO> getCustomerOrders(UUID customerId, Pageable pageable) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return orderRepository.findByCustomer(customer, pageable)
                .map(OrderDTO::fromEntity);
    }

    public Page<OrderDTO> getStoreOrders(UUID storeId, OrderStatus status, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (status != null) {
            return orderRepository.findByStoreAndStatus(store, status, pageable)
                    .map(OrderDTO::fromEntity);
        } else {
            return orderRepository.findByStore(store, pageable)
                    .map(OrderDTO::fromEntity);
        }
    }

    public OrderDTO getOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public StoreRatingDTO rateStore(UUID orderId, Integer rating, String review) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate order is delivered
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Can only rate stores for delivered orders");
        }

        // Check if already rated
        if (storeRatingRepository.existsByCustomerAndOrder(order.getCustomer(), order)) {
            throw new RuntimeException("You have already rated this store for this order");
        }

        // Validate rating
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // Create rating
        StoreRating storeRating = StoreRating.builder()
                .store(order.getStore())
                .customer(order.getCustomer())
                .order(order)
                .rating(rating)
                .review(review)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isVerifiedPurchase(true)
                .build();
        storeRating = storeRatingRepository.save(storeRating);

        // Update store rating
        updateStoreRating(order.getStore().getId());

        return StoreRatingDTO.fromEntity(storeRating);
    }

    @Transactional
    public void updateStoreRating(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Double averageRating = storeRatingRepository.findAverageRatingByStore(store);
        Long totalRatings = storeRatingRepository.countByStore(store);

        store.setRating(averageRating != null ? averageRating : 0.0);
        store.setTotalRatings(totalRatings.intValue());
        storeRepository.save(store);
    }

    public Page<StoreRatingDTO> getStoreRatings(UUID storeId, Pageable pageable) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        return storeRatingRepository.findByStore(store, pageable)
                .map(StoreRatingDTO::fromEntity);
    }
}
