package Commerce360.controller;

import Commerce360.dto.PaymentDTO;
import Commerce360.dto.PaymentIntentRequest;
import Commerce360.dto.PaymentIntentResponse;
import Commerce360.entity.Payment;
import Commerce360.service.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Stripe payment processing for customer orders (cards only, USD)")
public class PaymentController {

    private final StripePaymentService paymentService;

    @PostMapping("/create-intent")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create Payment Intent", description = "Create a Stripe payment intent for order checkout. Returns clientSecret for frontend payment confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment intent created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or insufficient amount"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request) {
        try {
            PaymentIntent intent = paymentService.createPaymentIntent(
                    request.getCustomerId(),
                    request.getAmount(),
                    request.getDescription());

            PaymentIntentResponse response = PaymentIntentResponse.builder()
                    .paymentIntentId(intent.getId())
                    .clientSecret(intent.getClientSecret())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(intent.getStatus())
                    .build();

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Stripe error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/confirm/{intentId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Confirm Payment", description = "Confirm payment after customer completes payment on frontend. Updates local payment record with Stripe status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment confirmed"),
            @ApiResponse(responseCode = "400", description = "Payment intent not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Stripe API error")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> confirmPayment(
            @Parameter(description = "Stripe payment intent ID") @PathVariable String intentId) {
        try {
            Payment payment = paymentService.confirmPayment(intentId);
            return ResponseEntity.ok(PaymentDTO.fromEntity(payment));

        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Stripe error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STORE_MANAGER')")
    @Operation(summary = "Get Payment Details", description = "Retrieve payment details by payment ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getPayment(
            @Parameter(description = "Payment ID") @PathVariable UUID id) {
        try {
            // This would require a findById method in the service
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                    .body("Method not yet implemented");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'STORE_MANAGER')")
    @Operation(summary = "Get Payment by Order", description = "Retrieve payment details for a specific order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found"),
            @ApiResponse(responseCode = "404", description = "Payment not found for this order"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getPaymentByOrder(
            @Parameter(description = "Order ID") @PathVariable UUID orderId) {
        try {
            Payment payment = paymentService.findByOrderId(orderId);
            if (payment == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(PaymentDTO.fromEntity(payment));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }
}
