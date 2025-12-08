package Commerce360.dto;

import Commerce360.entity.PurchaseOrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItemDTO {
    private UUID id;
    private UUID purchaseOrderId;
    private UUID supplierProductId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Integer receivedQuantity;

    public static PurchaseOrderItemDTO fromEntity(PurchaseOrderItem item) {
        return PurchaseOrderItemDTO.builder()
                .id(item.getId())
                .purchaseOrderId(item.getPurchaseOrder() != null ? item.getPurchaseOrder().getId() : null)
                .supplierProductId(item.getSupplierProduct() != null ? item.getSupplierProduct().getId() : null)
                .productName(item.getProductName())
                .productSku(item.getProductSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .receivedQuantity(item.getReceivedQuantity())
                .build();
    }
}
