package Commerce360.dto;

import Commerce360.entity.Cart;
import Commerce360.entity.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private UUID id;
    private UUID customerId;
    private UUID storeId;
    private String storeName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CartStatus status;
    private BigDecimal totalAmount;
    private Integer totalItems;
    
    @Builder.Default
    private List<CartItemDTO> items = new ArrayList<>();

    public static CartDTO fromEntity(Cart cart) {
        List<CartItemDTO> items = cart.getCartItems() != null 
            ? cart.getCartItems().stream()
                .map(CartItemDTO::fromEntity)
                .collect(Collectors.toList())
            : new ArrayList<>();
        
        BigDecimal totalAmount = items.stream()
            .map(CartItemDTO::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Integer totalItems = items.stream()
            .mapToInt(CartItemDTO::getQuantity)
            .sum();
        
        return CartDTO.builder()
                .id(cart.getId())
                .customerId(cart.getCustomer() != null ? cart.getCustomer().getId() : null)
                .storeId(cart.getStore() != null ? cart.getStore().getId() : null)
                .storeName(cart.getStore() != null ? cart.getStore().getName() : null)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .status(cart.getStatus())
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .items(items)
                .build();
    }
}
