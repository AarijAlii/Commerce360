package Commerce360.controller;

import Commerce360.dto.CustomerDTO;
import Commerce360.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer registration and profile management")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    @Operation(
        summary = "Register Customer",
        description = "Register new customer. **Automatically approved** (no admin approval needed)."
    )
    @SecurityRequirement(name = "")
    public ResponseEntity<CustomerDTO> registerCustomer(
            @Parameter(description = "Email address") @RequestParam String email,
            @Parameter(description = "Password") @RequestParam String password,
            @Parameter(description = "First name") @RequestParam String firstName,
            @Parameter(description = "Last name") @RequestParam String lastName,
            @Parameter(description = "Phone number") @RequestParam String phoneNumber,
            @Parameter(description = "Shipping address") @RequestParam String shippingAddress,
            @Parameter(description = "Billing address (optional)") @RequestParam(required = false) String billingAddress,
            @Parameter(description = "City") @RequestParam String city,
            @Parameter(description = "Postal code") @RequestParam String postalCode) {

        CustomerDTO customer = customerService.registerCustomer(
                email, password, firstName, lastName, phoneNumber,
                shippingAddress, billingAddress, city, postalCode);
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDTO> getCurrentProfile() {
        CustomerDTO customer = customerService.getCurrentCustomerProfile();
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<CustomerDTO> getCustomerProfile(@PathVariable UUID id) {
        CustomerDTO customer = customerService.getCustomerProfile(id);
        return ResponseEntity.ok(customer);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerDTO> updateProfile(
            @PathVariable UUID id,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) String shippingAddress,
            @RequestParam(required = false) String billingAddress,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String postalCode) {

        CustomerDTO customer = customerService.updateProfile(
                id, phoneNumber, shippingAddress, billingAddress, city, postalCode);
        return ResponseEntity.ok(customer);
    }
}
