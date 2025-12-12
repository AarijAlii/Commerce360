package Commerce360.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRegistrationRequest {

    // User fields
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    // Supplier-specific fields
    @NotBlank(message = "Company name is required")
    private String companyName;

    private String businessLicense;
    private String taxId;
    private String description;
    private String contact;
    private String address;
    private String city;
    private String country;
}
