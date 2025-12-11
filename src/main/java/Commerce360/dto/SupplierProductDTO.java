package Commerce360.dto;

import Commerce360.entity.SupplierProduct;
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
public class SupplierProductDTO {
    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private UUID productId;
    private String productName;
    private String productSku;
    private String supplierSku;
    private BigDecimal supplierPrice;
    private Integer minimumOrderQuantity;
    private Integer stockAvailable;
    private Integer leadTimeDays;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String imageUrl;

    public static SupplierProductDTO fromEntity(SupplierProduct supplierProduct) {
        return SupplierProductDTO.builder()
                .id(supplierProduct.getId())
                .supplierId(supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getId() : null)
                .supplierName(
                        supplierProduct.getSupplier() != null ? supplierProduct.getSupplier().getCompanyName() : null)
                .productId(supplierProduct.getProduct() != null ? supplierProduct.getProduct().getId() : null)
                .productName(supplierProduct.getProduct() != null ? supplierProduct.getProduct().getName() : null)
                .productSku(supplierProduct.getProduct() != null ? supplierProduct.getProduct().getSku() : null)
                .supplierSku(supplierProduct.getSupplierSku())
                .supplierPrice(supplierProduct.getSupplierPrice())
                .minimumOrderQuantity(supplierProduct.getMinimumOrderQuantity())
                .stockAvailable(supplierProduct.getStockAvailable())
                .leadTimeDays(supplierProduct.getLeadTimeDays())
                .isActive(supplierProduct.getIsActive())
                .createdAt(supplierProduct.getCreatedAt())
                .updatedAt(supplierProduct.getUpdatedAt())
                .description(supplierProduct.getDescription())
                .imageUrl(supplierProduct.getImageUrl())
                .build();
    }
}
