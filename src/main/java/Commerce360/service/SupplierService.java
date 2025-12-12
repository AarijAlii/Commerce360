package Commerce360.service;

import Commerce360.entity.Supplier;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.SupplierRepository;
import Commerce360.repository.UserRepository;
import Commerce360.security.SecurityContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Commerce360.dto.SupplierDTO;
import Commerce360.entity.ApprovalStatus;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class SupplierService {
    @Autowired
    private final SupplierRepository supplierRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    public SupplierService(SupplierRepository supplierRepository, UserRepository userRepository,
            SecurityContextUtil securityContextUtil, PasswordEncoder passwordEncoder) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.securityContextUtil = securityContextUtil;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Register new supplier with complete details
     * Creates both User (base) and Supplier (role-specific) entities
     */
    @Transactional
    public SupplierDTO registerSupplier(String email, String password, String firstName, String lastName,
            String companyName, String businessLicense, String taxId, String description,
            String contact, String address, String city, String country) {

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create base User entity
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .role(UserRole.SUPPLIER)
                .approvalStatus(ApprovalStatus.PENDING) // Needs admin approval
                .registrationDate(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // Create Supplier entity with all required fields
        Supplier supplier = Supplier.builder()
                .user(user)
                .companyName(companyName)
                .businessLicense(businessLicense)
                .taxId(taxId)
                .description(description)
                .contact(contact)
                .address(address)
                .city(city)
                .country(country)
                .isActive(true)
                .rating(0.0)
                .totalOrders(0)
                .build();
        supplier = supplierRepository.save(supplier);

        return SupplierDTO.fromEntity(supplier);
    }

    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Optional<Supplier> getSupplierById(UUID id) {
        return supplierRepository.findById(id);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        // Check if the current user is an admin
        User currentUser = userRepository.findById(securityContextUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can create suppliers");
        }

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Optional<Supplier> updateSupplier(UUID id, Supplier supplier) {
        if (!supplierRepository.existsById(id)) {
            return Optional.empty();
        }
        supplier.setId(id);
        return Optional.of(supplierRepository.save(supplier));
    }

    @Transactional
    public boolean deleteSupplier(UUID id) {
        if (!supplierRepository.existsById(id)) {
            return false;
        }
        supplierRepository.deleteById(id);
        return true;
    }

    public Page<Supplier> searchSuppliers(String query, Pageable pageable) {
        return supplierRepository.findByCompanyNameContainingIgnoreCaseOrUser_EmailContainingIgnoreCase(query, query,
                pageable);
    }

    /**
     * Get current supplier's profile
     */
    public SupplierDTO getCurrentSupplierProfile() {
        User currentUser = securityContextUtil.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"));
        
        Supplier supplier = supplierRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Supplier profile not found"));
        
        return SupplierDTO.fromEntity(supplier);
    }

    /**
     * Supplier updates own profile
     */
    @Transactional
    public SupplierDTO updateOwnProfile(String companyName, String businessLicense,
            String taxId, String description, String contact, String address,
            String city, String country) {
        
        User currentUser = securityContextUtil.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("Not authenticated"));
        
        Supplier supplier = supplierRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Supplier profile not found"));

        if (companyName != null)
            supplier.setCompanyName(companyName);
        if (businessLicense != null)
            supplier.setBusinessLicense(businessLicense);
        if (taxId != null)
            supplier.setTaxId(taxId);
        if (description != null)
            supplier.setDescription(description);
        if (contact != null)
            supplier.setContact(contact);
        if (address != null)
            supplier.setAddress(address);
        if (city != null)
            supplier.setCity(city);
        if (country != null)
            supplier.setCountry(country);

        supplier = supplierRepository.save(supplier);
        return SupplierDTO.fromEntity(supplier);
    }
}