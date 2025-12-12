package Commerce360.service;

import Commerce360.dto.StoreManagerDTO;
import Commerce360.entity.StoreManager;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.entity.ApprovalStatus;
import Commerce360.repository.StoreManagerRepository;
import Commerce360.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class StoreManagerService {

    @Autowired
    private StoreManagerRepository storeManagerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register new store manager
     * Creates both User (base) and StoreManager (role-specific) entities
     */
    @Transactional
    public StoreManagerDTO registerStoreManager(String email, String password,
            String firstName, String lastName, String phoneNumber) {

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
                .role(UserRole.STORE_MANAGER)
                .approvalStatus(ApprovalStatus.PENDING) // Needs admin approval
                .registrationDate(LocalDateTime.now())
                .build();
        user = userRepository.save(user);

        // Create StoreManager entity
        StoreManager storeManager = StoreManager.builder()
                .user(user)
                .phoneNumber(phoneNumber)
                .isActive(true)
                .build();
        storeManager = storeManagerRepository.save(storeManager);

        return StoreManagerDTO.fromEntity(storeManager);
    }

    public Optional<StoreManager> getStoreManagerById(UUID id) {
        return storeManagerRepository.findById(id);
    }

    public Optional<StoreManager> getStoreManagerByUserId(UUID userId) {
        return storeManagerRepository.findByUserId(userId);
    }
}
