package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String companyName;

    @Column
    private String businessLicense;

    @Column
    private String taxId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String contact;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column
    private String city;

    @Column
    private String country;

    // Approval status managed via User entity (user.approvalStatus,
    // user.approvalDate, etc.)

    @Column
    @Builder.Default
    private Boolean isActive = true;

    @Column
    @Builder.Default
    private Double rating = 0.0;

    @Column
    @Builder.Default
    private Integer totalOrders = 0;
}