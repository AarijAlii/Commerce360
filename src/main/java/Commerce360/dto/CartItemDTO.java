package Commerce360.dto;

import Commerce360.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private UUID id;
    private UUID cartId;
    private UUID productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private String imageUrl;
    private Boolean isAvailable;

    public static CartItemDTO fromEntity(CartItem cartItem) {
        BigDecimal unitPrice = cartItem.getUnitPrice();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return CartItemDTO.builder()
                .id(cartItem.getId())
                .cartId(cartItem.getCart() != null ? cartItem.getCart().getId() : null)
                .productId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null)
                .productName(cartItem.getProduct() != null ? cartItem.getProduct().getName() : null)
                .productSku(cartItem.getProduct() != null ? cartItem.getProduct().getSku() : null)
                .quantity(cartItem.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .addedAt(cartItem.getAddedAt())
                .updatedAt(cartItem.getUpdatedAt())
                .imageUrl(cartItem.getProduct() != null ? cartItem.getProduct().getImageUrl() : null)
                .isAvailable(cartItem.getProduct() != null ? cartItem.getProduct().getIsAvailableToCustomers() : false)
                .build();
    }
}
