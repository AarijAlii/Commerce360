package Commerce360.dto;

import Commerce360.entity.Product;
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
public class ProductDTO {
    private UUID id;
    private String sku;
    private String name;
    private String category;
    private BigDecimal price;
    private SupplierDTO supplier;

    public static ProductDTO fromEntity(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .supplier(product.getSupplier() != null ? SupplierDTO.fromEntity(product.getSupplier()) : null)
                .build();
    }
}