package Commerce360.repository;

import Commerce360.entity.StoreManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreManagerRepository extends JpaRepository<StoreManager, UUID> {

    /**
     * Find StoreManager by associated User ID
     */
    Optional<StoreManager> findByUserId(UUID userId);

    /**
     * Find StoreManager by User email
     */
    Optional<StoreManager> findByUser_Email(String email);

    /**
     * Check if StoreManager exists for User
     */
    boolean existsByUserId(UUID userId);
}
