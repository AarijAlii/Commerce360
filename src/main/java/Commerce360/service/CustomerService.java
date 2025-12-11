package Commerce360.service;

import Commerce360.dto.CustomerDTO;
import Commerce360.entity.Customer;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.CustomerRepository;
import Commerce360.repository.UserRepository;
import Commerce360.security.SecurityContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SecurityContextUtil securityContextUtil;

    @Transactional
    public CustomerDTO registerCustomer(String email, String password, String firstName, String lastName,
            String phoneNumber, String shippingAddress, String billingAddress,
            String city, String postalCode) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create user account (auto-approved for customers)
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.CUSTOMER)
                .approvalStatus(Commerce360.entity.ApprovalStatus.APPROVED) // Auto-approve customers
                .registrationDate(LocalDateTime.now())
                .approvalDate(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // Create customer profile
        Customer customer = Customer.builder()
                .user(user)
                .phoneNumber(phoneNumber)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress != null ? billingAddress : shippingAddress)
                .city(city)
                .postalCode(postalCode)
                .createdAt(LocalDateTime.now())
                .totalOrders(0)
                .isActive(true)
                .build();
        customer = customerRepository.save(customer);

        return CustomerDTO.fromEntity(customer);
    }

    @Transactional
    public CustomerDTO updateProfile(UUID customerId, String phoneNumber, String shippingAddress,
            String billingAddress, String city, String postalCode) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (phoneNumber != null)
            customer.setPhoneNumber(phoneNumber);
        if (shippingAddress != null)
            customer.setShippingAddress(shippingAddress);
        if (billingAddress != null)
            customer.setBillingAddress(billingAddress);
        if (city != null)
            customer.setCity(city);
        if (postalCode != null)
            customer.setPostalCode(postalCode);

        customer = customerRepository.save(customer);
        return CustomerDTO.fromEntity(customer);
    }

    public CustomerDTO getCustomerProfile(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return CustomerDTO.fromEntity(customer);
    }

    public CustomerDTO getCurrentCustomerProfile() {
        User currentUser = securityContextUtil.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"));

        Customer customer = customerRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Customer profile not found"));

        return CustomerDTO.fromEntity(customer);
    }

    @Transactional
    public void incrementOrderCount(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        customer.setTotalOrders(customer.getTotalOrders() + 1);
        customer.setLastOrderDate(LocalDateTime.now());
        customerRepository.save(customer);
    }
}
