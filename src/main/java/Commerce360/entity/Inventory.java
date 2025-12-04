package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private Double unitPrice;

    @Column
    private String batchNumber;

    @Column
    private String notes;

    public Inventory(Store store, Product product, int quantity) {
        this.store = store;
        this.product = product;
        this.quantity = quantity;
    }
}
