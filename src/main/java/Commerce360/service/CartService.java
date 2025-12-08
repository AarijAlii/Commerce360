package Commerce360.service;

import Commerce360.dto.CartDTO;
import Commerce360.entity.*;
import Commerce360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Transactional
    public CartDTO addToCart(UUID customerId, UUID storeId, UUID productId, Integer quantity) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Validate product is available to customers
        if (!product.getIsAvailableToCustomers()) {
            throw new RuntimeException("Product is not available for purchase");
        }

        // Check inventory availability
        Inventory inventory = inventoryRepository.findByStoreAndProduct(store, product)
                .orElseThrow(() -> new RuntimeException("Product not available at this store"));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + inventory.getAvailableQuantity());
        }

        // Get or create active cart for this customer and store
        Cart cart = cartRepository.findByCustomerAndStoreAndStatus(customer, store, CartStatus.ACTIVE)
                .orElse(Cart.builder()
                        .customer(customer)
                        .store(store)
                        .createdAt(LocalDateTime.now())
                        .status(CartStatus.ACTIVE)
                        .build());

        if (cart.getId() == null) {
            cart = cartRepository.save(cart);
        }

        // Check if item already in cart
        CartItem cartItem = cartItemRepository.findByCartAndProduct(cart, product)
                .orElse(null);

        if (cartItem != null) {
            // Update quantity
            int newQuantity = cartItem.getQuantity() + quantity;
            if (inventory.getAvailableQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock for requested quantity");
            }
            cartItem.setQuantity(newQuantity);
            cartItem.setUpdatedAt(LocalDateTime.now());
        } else {
            // Add new item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
        }

        cartItemRepository.save(cartItem);
        cart.setUpdatedAt(LocalDateTime.now());
        cart = cartRepository.save(cart);

        return CartDTO.fromEntity(cart);
    }

    @Transactional
    public CartDTO updateCartItem(UUID cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            throw new RuntimeException("Quantity must be greater than 0");
        }

        // Check inventory
        Inventory inventory = inventoryRepository
                .findByStoreAndProduct(cartItem.getCart().getStore(), cartItem.getProduct())
                .orElseThrow(() -> new RuntimeException("Product not available"));

        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + inventory.getAvailableQuantity());
        }

        cartItem.setQuantity(quantity);
        cartItem.setUpdatedAt(LocalDateTime.now());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return CartDTO.fromEntity(cart);
    }

    @Transactional
    public CartDTO removeFromCart(UUID cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return CartDTO.fromEntity(cart);
    }

    @Transactional
    public void clearCart(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCart(cart);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    public CartDTO getCart(UUID customerId, UUID storeId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Cart cart = cartRepository.findByCustomerAndStoreAndStatus(customer, store, CartStatus.ACTIVE)
                .orElse(Cart.builder()
                        .customer(customer)
                        .store(store)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .status(CartStatus.ACTIVE)
                        .build());

        return CartDTO.fromEntity(cart);
    }

    public List<CartDTO> getCustomerCarts(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return cartRepository.findByCustomerAndStatus(customer, CartStatus.ACTIVE).stream()
                .map(CartDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markCartAsConverted(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.setStatus(CartStatus.CONVERTED);
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }
}
