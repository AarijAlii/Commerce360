package Commerce360.controller;

import Commerce360.dto.CartDTO;
import Commerce360.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> addToCart(
            @RequestParam UUID customerId,
            @RequestParam UUID storeId,
            @RequestParam UUID productId,
            @RequestParam Integer quantity) {

        CartDTO cart = cartService.addToCart(customerId, storeId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable UUID itemId,
            @RequestParam Integer quantity) {

        CartDTO cart = cartService.updateCartItem(itemId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> removeFromCart(@PathVariable UUID itemId) {
        CartDTO cart = cartService.removeFromCart(itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{cartId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
        cartService.clearCart(cartId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CartDTO> getCart(
            @RequestParam UUID customerId,
            @RequestParam UUID storeId) {

        CartDTO cart = cartService.getCart(customerId, storeId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<List<CartDTO>> getCustomerCarts(@PathVariable UUID customerId) {
        List<CartDTO> carts = cartService.getCustomerCarts(customerId);
        return ResponseEntity.ok(carts);
    }
}
