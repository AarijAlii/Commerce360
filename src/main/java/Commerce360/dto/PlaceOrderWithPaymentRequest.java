package Commerce360.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderWithPaymentRequest {
    
    private UUID cartId;
    private String paymentIntentId;  // Stripe payment intent ID (already confirmed)
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String contactPhone;
    private String notes;
}
