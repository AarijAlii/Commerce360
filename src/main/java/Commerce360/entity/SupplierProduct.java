package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "supplier_products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(unique = true)
    private String supplierSku; // Supplier's internal SKU

    @Column(nullable = false)
    private BigDecimal supplierPrice; // Price at which supplier sells to stores

    @Column
    @Builder.Default
    private Integer minimumOrderQuantity = 1;

    @Column
    @Builder.Default
    private Integer stockAvailable = 0;

    @Column
    @Builder.Default
    private Integer leadTimeDays = 7; // Delivery time in days

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String imageUrl;
}
