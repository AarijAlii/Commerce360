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

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderDTO> placeOrder(
            @RequestParam UUID cartId,
            @RequestParam String shippingAddress,
            @RequestParam String shippingCity,
            @RequestParam String shippingPostalCode,
            @RequestParam String contactPhone,
            @RequestParam(required = false) String notes) {

        OrderDTO order = orderService.placeOrder(
                cartId, shippingAddress, shippingCity, shippingPostalCode, contactPhone, notes);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> confirmOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.confirmOrder(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/processing")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> markAsProcessing(@PathVariable UUID id) {
        OrderDTO order = orderService.markAsProcessing(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> markAsShipped(@PathVariable UUID id) {
        OrderDTO order = orderService.markAsShipped(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/deliver")
    @PreAuthorize("hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> markAsDelivered(@PathVariable UUID id) {
        OrderDTO order = orderService.markAsDelivered(id);
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('STORE_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> cancelOrder(
            @PathVariable UUID id,
            @RequestParam String reason) {

        OrderDTO order = orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
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
    public ResponseEntity<OrderDTO> getOrder(@PathVariable UUID id) {
        OrderDTO order = orderService.getOrder(id);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/rate")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<StoreRatingDTO> rateStore(
            @PathVariable UUID orderId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String review) {

        StoreRatingDTO storeRating = orderService.rateStore(orderId, rating, review);
        return ResponseEntity.ok(storeRating);
    }

    @GetMapping("/store/{storeId}/ratings")
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
