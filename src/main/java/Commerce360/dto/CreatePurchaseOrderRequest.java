package Commerce360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePurchaseOrderRequest {
    private UUID storeId;
    private UUID supplierId;
    private LocalDateTime expectedDeliveryDate;
    private String notes;

    @Builder.Default
    private List<PurchaseOrderItemRequest> items = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseOrderItemRequest {
        private UUID supplierProductId;
        private Integer quantity;
    }
}
