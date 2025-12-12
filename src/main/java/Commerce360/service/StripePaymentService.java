package Commerce360.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import Commerce360.entity.Customer;
import Commerce360.entity.Payment;
import Commerce360.entity.PaymentStatus;
import Commerce360.repository.CustomerRepository;
import Commerce360.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripePaymentService {
    
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    
    @Value("${stripe.currency}")
    private String defaultCurrency;
    
    @Value("${stripe.payment.description}")
    private String paymentDescription;
    
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }
    
    /**
     * Create a payment intent for order payment
     * Stripe amounts are in cents, so $10.00 = 1000
     */
    @Transactional
    public PaymentIntent createPaymentIntent(UUID customerId, BigDecimal amount, String description) 
            throws StripeException {
        
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        
        // Create Stripe payment intent
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(defaultCurrency)
                .setDescription(description != null ? description : paymentDescription)
                .putMetadata("customerId", customerId.toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();
        
        PaymentIntent intent = PaymentIntent.create(params);
        
        // Save payment record in database
        Payment payment = Payment.builder()
                .stripePaymentIntentId(intent.getId())
                .clientSecret(intent.getClientSecret())
                .amount(amount)
                .currency(defaultCurrency)
                .status(PaymentStatus.PENDING)
                .customer(customer)
                .build();
        
        paymentRepository.save(payment);
        
        return intent;
    }
    
    /**
     * Retrieve payment intent from Stripe
     */
    public PaymentIntent retrievePaymentIntent(String intentId) throws StripeException {
        return PaymentIntent.retrieve(intentId);
    }
    
    /**
     * Confirm payment and update local payment record
     * Called after customer completes payment on frontend
     */
    @Transactional
    public Payment confirmPayment(String intentId) throws StripeException {
        // Retrieve latest status from Stripe
        PaymentIntent intent = PaymentIntent.retrieve(intentId);
        
        // Find local payment record
        Payment payment = paymentRepository.findByStripePaymentIntentId(intentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        // Map Stripe status to our status
        PaymentStatus status = mapStripeStatus(intent.getStatus());
        payment.setStatus(status);
        
        // If payment succeeded, record details
        if ("succeeded".equals(intent.getStatus())) {
            // Get latest charge ID from the intent
            if (intent.getLatestCharge() != null) {
                payment.setStripeChargeId(intent.getLatestCharge());
                // Receipt URL requires fetching the charge object
            }
            payment.setPaidAt(LocalDateTime.now());
        } else if ("failed".equals(intent.getStatus())) {
            payment.setFailureReason(intent.getLastPaymentError() != null ? 
                    intent.getLastPaymentError().getMessage() : "Payment failed");
        }
        
        return paymentRepository.save(payment);
    }
    
    /**
     * Find payment by payment intent ID
     */
    public Payment findByIntentId(String intentId) {
        return paymentRepository.findByStripePaymentIntentId(intentId)
                .orElse(null);
    }
    
    /**
     * Find payment by order ID
     */
    public Payment findByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElse(null);
    }
    
    /**
     * Map Stripe payment intent status to our PaymentStatus enum
     */
    private PaymentStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "succeeded" -> PaymentStatus.SUCCEEDED;
            case "processing" -> PaymentStatus.PROCESSING;
            case "requires_action" -> PaymentStatus.REQUIRES_ACTION;
            case "canceled" -> PaymentStatus.CANCELED;
            case "requires_payment_method", "requires_confirmation" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }
}
