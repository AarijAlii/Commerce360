package Commerce360.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Commerce360.entity.Inventory;
import Commerce360.entity.Store;
import Commerce360.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, UUID>, JpaSpecificationExecutor<Inventory> {
    List<Inventory> findByStore(Store store);

    Optional<Inventory> findByStoreAndProduct(Store store, Product product);

    List<Inventory> findByExpiryDateBefore(LocalDateTime date);

    Page<Inventory> findByStoreAndProductCategory(Store store, String category, Pageable pageable);
}
