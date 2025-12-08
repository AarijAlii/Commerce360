package Commerce360.controller;

import Commerce360.dto.CustomerDTO;
import Commerce360.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerDTO> registerCustomer(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String phoneNumber,
            @RequestParam String shippingAddress,
            @RequestParam(required = false) String billingAddress,
            @RequestParam String city,
            @RequestParam String postalCode) {

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
