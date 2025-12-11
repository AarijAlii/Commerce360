package Commerce360.dto;

import Commerce360.entity.StoreRating;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreRatingDTO {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private UUID customerId;
    private String customerName;
    private UUID orderId;
    private String orderNumber;
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isVerifiedPurchase;

    public static StoreRatingDTO fromEntity(StoreRating storeRating) {
        return StoreRatingDTO.builder()
                .id(storeRating.getId())
                .storeId(storeRating.getStore() != null ? storeRating.getStore().getId() : null)
                .storeName(storeRating.getStore() != null ? storeRating.getStore().getName() : null)
                .customerId(storeRating.getCustomer() != null ? storeRating.getCustomer().getId() : null)
                .customerName(storeRating.getCustomer() != null && storeRating.getCustomer().getUser() != null
                        ? storeRating.getCustomer().getUser().getFirstName() + " "
                                + storeRating.getCustomer().getUser().getLastName()
                        : null)
                .orderId(storeRating.getOrder() != null ? storeRating.getOrder().getId() : null)
                .orderNumber(storeRating.getOrder() != null ? storeRating.getOrder().getOrderNumber() : null)
                .rating(storeRating.getRating())
                .review(storeRating.getReview())
                .createdAt(storeRating.getCreatedAt())
                .updatedAt(storeRating.getUpdatedAt())
                .isVerifiedPurchase(storeRating.getIsVerifiedPurchase())
                .build();
    }
}
