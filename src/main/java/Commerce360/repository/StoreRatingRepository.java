package Commerce360.repository;

import Commerce360.entity.Customer;
import Commerce360.entity.Order;
import Commerce360.entity.Store;
import Commerce360.entity.StoreRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StoreRatingRepository extends JpaRepository<StoreRating, UUID> {
    Page<StoreRating> findByStore(Store store, Pageable pageable);

    List<StoreRating> findByCustomer(Customer customer);

    Optional<StoreRating> findByCustomerAndOrder(Customer customer, Order order);

    @Query("SELECT AVG(sr.rating) FROM StoreRating sr WHERE sr.store = :store")
    Double findAverageRatingByStore(@Param("store") Store store);

    Long countByStore(Store store);

    boolean existsByCustomerAndOrder(Customer customer, Order order);
}
