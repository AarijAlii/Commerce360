package Commerce360.repository;

import Commerce360.entity.Customer;
import Commerce360.entity.Order;
import Commerce360.entity.OrderStatus;
import Commerce360.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByCustomer(Customer customer, Pageable pageable);

    Page<Order> findByStore(Store store, Pageable pageable);

    Page<Order> findByStoreAndStatus(Store store, OrderStatus status, Pageable pageable);

    List<Order> findByCustomerAndStatus(Customer customer, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.store = :store AND o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByStoreAndDateRange(@Param("store") Store store,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Long countByStoreAndStatus(Store store, OrderStatus status);
}
