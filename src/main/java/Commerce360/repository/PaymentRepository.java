package Commerce360.repository;

import Commerce360.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
    
    Optional<Payment> findByOrderId(UUID orderId);
}
