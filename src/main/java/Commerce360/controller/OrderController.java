package Commerce360.controller;

import Commerce360.dto.OrderDTO;
import Commerce360.dto.StoreRatingDTO;
import Commerce360.entity.OrderStatus;
import Commerce360.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Customer Orders (B2C)", description = "Customer order lifecycle management - place, track, rate")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place Order", description = "Convert cart to order. Automatically reserves inventory and marks cart as converted.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order placed successfully"),
            @ApiResponse(responseCode = "400", description = "Cart invalid or insufficient stock"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    public ResponseEntity<OrderDTO> placeOrder(
            @Parameter(description = "Cart ID to convert") @RequestParam UUID cartId,
            @Parameter(description = "Shipping address") @RequestParam String shippingAddress,
            @Parameter(description = "Shipping city") @RequestParam String shippingCity,
            @Parameter(description = "Shipping postal code") @RequestParam String shippingPostalCode,
            @Parameter(description = "Contact phone number") @RequestParam String contactPhone,
            @Parameter(description = "Optional delivery notes") @RequestParam(required = false) String notes) {

        OrderDTO order = orderService.placeOrder(
                cartId, shippingAddress, shippingCity, shippingPostalCode, contactPhone, notes);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Confirm Order", description = "Store manager confirms order after customer payment (STORE_MANAGER or ADMIN)")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/processing")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Mark Order as Processing", description = "Update order status to processing - store is preparing order (STORE_MANAGER or ADMIN)")
    public ResponseEntity<OrderDTO> markAsProcessing(@PathVariable UUID id) {
        OrderDTO order = orderService.markAsProcessing(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Mark Order as Shipped", description = "Update order status to shipped - order is in transit (STORE_MANAGER or ADMIN)")
    public ResponseEntity<OrderDTO> markAsShipped(@PathVariable UUID id) {
        OrderDTO order = orderService.markAsShipped(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Mark Order as Delivered", description = "Mark order as delivered. **AUTOMATICALLY updates inventory** (reduces stock, releases reserved quantity) and creates transaction.")
    public ResponseEntity<OrderDTO> markAsDelivered(@Parameter(description = "Order ID") @PathVariable UUID id) {
        OrderDTO order = orderService.markAsDelivered(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Cancel Order", description = "Cancel order with reason. Can be done by customer or store manager")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {

        OrderDTO order = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get Customer's Orders", description = "Get all orders for a specific customer with pagination (CUSTOMER or ADMIN)")
    public ResponseEntity<Page<OrderDTO>> getCustomerOrders(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDTO> orders = orderService.getCustomerOrders(customerId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get Store's Orders", description = "Get all orders for a specific store, optionally filtered by status (STORE_MANAGER or ADMIN)")
    public ResponseEntity<Page<OrderDTO>> getStoreOrders(
            @PathVariable UUID storeId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderDTO> orders = orderService.getStoreOrders(storeId, status, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    @Operation(summary = "Get Order Details", description = "Get detailed information about a specific order")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Rate Store", description = "Rate a store after order delivery. **Automatically calculates average rating**. Only delivered orders can be rated.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rating submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Order not delivered or already rated"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<StoreRatingDTO> rateStore(
            @Parameter(description = "Order ID") @PathVariable UUID orderId,
            @Parameter(description = "Rating (1-5)") @RequestParam Integer rating,
            @Parameter(description = "Optional review text") @RequestParam(required = false) String review) {

        StoreRatingDTO storeRating = orderService.rateStore(orderId, rating, review);
        return ResponseEntity.ok(storeRating);
    }

    @GetMapping("/store/{storeId}/ratings")
    @Operation(summary = "Get Store Ratings", description = "Get all ratings and reviews for a specific store")
    public ResponseEntity<Page<StoreRatingDTO>> getStoreRatings(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StoreRatingDTO> ratings = orderService.getStoreRatings(storeId, pageable);
        return ResponseEntity.ok(ratings);
    }
}
