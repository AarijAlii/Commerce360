package Commerce360.controller;

import Commerce360.dto.CartDTO;
import Commerce360.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Shopping Cart (B2C)", description = "Multi-store shopping cart management for customers")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add Product to Cart", description = "Add product to cart for specific store. Validates inventory availability.")
    public ResponseEntity<CartDTO> addToCart(
            @Parameter(description = "Customer ID") @RequestParam UUID customerId,
            @Parameter(description = "Store ID") @RequestParam UUID storeId,
            @Parameter(description = "Product ID") @RequestParam UUID productId,
            @Parameter(description = "Quantity") @RequestParam Integer quantity) {

        CartDTO cart = cartService.addToCart(customerId, storeId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update Cart Item Quantity", description = "Update quantity of an existing cart item (CUSTOMER only)")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID itemId,
            @RequestParam Integer quantity) {

        CartDTO cart = cartService.updateCartItem(itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Remove Item from Cart", description = "Remove a specific item from cart (CUSTOMER only)")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable UUID itemId) {
        CartDTO cart = cartService.removeFromCart(itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Clear Cart", description = "Remove all items from cart (CUSTOMER only)")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get Cart for Store", description = "Get customer's cart for a specific store (CUSTOMER only)")
    public ResponseEntity<CartDTO> getCart(
            @RequestParam UUID customerId,
            @RequestParam UUID storeId) {

        CartDTO cart = cartService.getCart(customerId, storeId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get All Customer Carts", description = "Get all active carts for a customer across all stores (CUSTOMER or ADMIN)")
    public ResponseEntity<List<CartDTO>> getCustomerCarts(@PathVariable UUID customerId) {
        List<CartDTO> carts = cartService.getCustomerCarts(customerId);
        return ResponseEntity.ok(carts);
    }
}
