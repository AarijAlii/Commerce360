package Commerce360.dto;

import Commerce360.entity.Payment;
import Commerce360.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {

    private UUID id;
    private String stripePaymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private UUID orderId;
    private UUID customerId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private String failureReason;
    private String receiptUrl;

    public static PaymentDTO fromEntity(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDTO.builder()
                .id(payment.getId())
                .stripePaymentIntentId(payment.getStripePaymentIntentId())
                .clientSecret(payment.getClientSecret())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .customerId(payment.getCustomer() != null ? payment.getCustomer().getId() : null)
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .failureReason(payment.getFailureReason())
                .receiptUrl(payment.getReceiptUrl())
                .build();
    }
}
