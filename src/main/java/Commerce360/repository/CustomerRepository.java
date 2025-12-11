package Commerce360.repository;

import Commerce360.entity.Customer;
import Commerce360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByUser(User user);

    Optional<Customer> findByUserId(UUID userId);
}
