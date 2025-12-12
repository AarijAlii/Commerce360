package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Store {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private StoreManager owner; // Changed from User to StoreManager

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String imageUrl;

    @Column
    private String phoneNumber;

    @Column
    private String email;

    @Column
    @Builder.Default
    private Double rating = 0.0;

    @Column
    @Builder.Default
    private Integer totalRatings = 0;

    @Column
    @Builder.Default
    private Integer totalOrders = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
