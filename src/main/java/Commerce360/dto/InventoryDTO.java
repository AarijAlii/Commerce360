package Commerce360.dto;

import lombok.*;
import Commerce360.entity.Inventory;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private UUID productId;
    private String productName;
    private String productCategory;
    private Integer quantity;
    private LocalDateTime lastUpdated;
    private LocalDateTime expiryDate;
    private Double unitPrice;
    private String batchNumber;
    private String notes;

    public static InventoryDTO fromEntity(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .storeId(inventory.getStore().getId())
                .storeName(inventory.getStore().getName())
                .productId(inventory.getProduct().getId())
                .productName(inventory.getProduct().getName())
                .productCategory(inventory.getProduct().getCategory())
                .quantity(inventory.getQuantity())
                .lastUpdated(inventory.getLastUpdated())
                .expiryDate(inventory.getExpiryDate())
                .unitPrice(inventory.getUnitPrice())
                .batchNumber(inventory.getBatchNumber())
                .notes(inventory.getNotes())
                .build();
    }
}