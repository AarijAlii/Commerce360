package Commerce360.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalStatus approvalStatus;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    private LocalDateTime approvalDate;

    private String rejectionReason;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Store> ownedStores = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "user_managed_stores", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "store_id"))
    @Builder.Default
    private List<Store> managedStores = new ArrayList<>();

    public List<Store> getManagedStores() {
        return managedStores;
    }

    public void setManagedStores(List<Store> managedStores) {
        this.managedStores = managedStores;
    }
}
