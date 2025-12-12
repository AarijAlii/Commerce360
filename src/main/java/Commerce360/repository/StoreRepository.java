package Commerce360.repository;

import Commerce360.entity.Store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StoreRepository extends JpaRepository<Store, UUID> {

    /**
     * Find stores by StoreManager (owner) ID
     * Note: owner is now StoreManager entity, not User
     */
    List<Store> findByOwnerId(UUID ownerId); // ownerId is StoreManager.id
}
