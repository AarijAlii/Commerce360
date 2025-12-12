package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "store_managers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreManager {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // Store ownership (moved from User)
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Store> ownedStores = new ArrayList<>();
    
    // Store management (moved from User)
    @ManyToMany
    @JoinTable(
        name = "manager_managed_stores",
        joinColumns = @JoinColumn(name = "manager_id"),
        inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    @Builder.Default
    private List<Store> managedStores = new ArrayList<>();
    
    // Manager-specific fields
    @Column
    private String phoneNumber;
    
    @Column
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
