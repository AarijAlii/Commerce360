package Commerce360.dto;

import Commerce360.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {
    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String shippingAddress;
    private String billingAddress;
    private String city;
    private String postalCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastOrderDate;
    private Integer totalOrders;
    private Boolean isActive;

    public static CustomerDTO fromEntity(Customer customer) {
        return CustomerDTO.builder()
                .id(customer.getId())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .email(customer.getUser() != null ? customer.getUser().getEmail() : null)
                .firstName(customer.getUser() != null ? customer.getUser().getFirstName() : null)
                .lastName(customer.getUser() != null ? customer.getUser().getLastName() : null)
                .phoneNumber(customer.getPhoneNumber())
                .shippingAddress(customer.getShippingAddress())
                .billingAddress(customer.getBillingAddress())
                .city(customer.getCity())
                .postalCode(customer.getPostalCode())
                .createdAt(customer.getCreatedAt())
                .lastOrderDate(customer.getLastOrderDate())
                .totalOrders(customer.getTotalOrders())
                .isActive(customer.getIsActive())
                .build();
    }
}
