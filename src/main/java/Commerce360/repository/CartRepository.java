package Commerce360.repository;

import Commerce360.entity.Cart;
import Commerce360.entity.CartStatus;
import Commerce360.entity.Customer;
import Commerce360.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByCustomerAndStoreAndStatus(Customer customer, Store store, CartStatus status);

    List<Cart> findByCustomerAndStatus(Customer customer, CartStatus status);

    List<Cart> findByCustomer(Customer customer);

    List<Cart> findByStore(Store store);
}
