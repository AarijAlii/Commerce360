package Commerce360.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import Commerce360.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripePaymentService paymentService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Verify webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            log.info("Received Stripe webhook event: {}", event.getType());

        } catch (SignatureVerificationException e) {
            log.error("Invalid webhook signature", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            log.error("Webhook error", e);
            return ResponseEntity.badRequest().body("Webhook error: " + e.getMessage());
        }

        // Handle different event types
        switch (event.getType()) {
            case "payment_intent.succeeded":
                handlePaymentSuccess(event);
                break;

            case "payment_intent.payment_failed":
                handlePaymentFailure(event);
                break;

            case "payment_intent.canceled":
                handlePaymentCanceled(event);
                break;

            default:
                log.info("Unhandled event type: {}", event.getType());
                return ResponseEntity.ok("Unhandled event type");
        }

        return ResponseEntity.ok("Webhook processed");
    }

    private void handlePaymentSuccess(Event event) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (stripeObject instanceof PaymentIntent) {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                log.info("Payment succeeded: {}", intent.getId());

                // Update payment status
                paymentService.confirmPayment(intent.getId());

                // TODO: Trigger additional order processing if needed
                // e.g., auto-confirm order, send receipt email, etc.
            }
        } catch (Exception e) {
            log.error("Error handling payment success webhook", e);
        }
    }

    private void handlePaymentFailure(Event event) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (stripeObject instanceof PaymentIntent) {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                log.error("Payment failed: {}", intent.getId());

                // Update payment status
                paymentService.confirmPayment(intent.getId());

                // TODO: Notify customer of payment failure
            }
        } catch (Exception e) {
            log.error("Error handling payment failure webhook", e);
        }
    }

    private void handlePaymentCanceled(Event event) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
            if (stripeObject instanceof PaymentIntent) {
                PaymentIntent intent = (PaymentIntent) stripeObject;
                log.info("Payment canceled: {}", intent.getId());

                // Update payment status
                paymentService.confirmPayment(intent.getId());
            }
        } catch (Exception e) {
            log.error("Error handling payment cancellation webhook", e);
        }
    }
}
