package Commerce360.service;

// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import Commerce360.entity.User;
import Commerce360.repository.UserRepository;
import Commerce360.entity.UserRole;
import Commerce360.entity.ApprovalStatus;
import Commerce360.security.SecurityContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final StoreService storeService;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            StoreService storeService, SecurityContextUtil securityContextUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.storeService = storeService;
        this.securityContextUtil = securityContextUtil;
    }

    // @Cacheable(value = "users", key = "#email")
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // @CacheEvict(value = "users", key = "#user.email")
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerUser(String email, String password, UserRole role, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .approvalStatus(ApprovalStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    // @Cacheable(value = "users", key = "'role_' + #role")
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    // @Cacheable(value = "users", key = "'status_' + #status")
    public List<User> getUsersByStatus(ApprovalStatus status) {
        return userRepository.findByApprovalStatus(status);
    }

    public User approveUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setApprovalStatus(ApprovalStatus.APPROVED);
        user.setApprovalDate(LocalDateTime.now());

        return userRepository.save(user);
    }

    public User rejectUser(UUID userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setApprovalStatus(ApprovalStatus.REJECTED);
        user.setRejectionReason(reason);

        return userRepository.save(user);
    }

    // @CacheEvict(value = "users", key = "#user.email")
    public User updateUser(UUID userId, User updatedUser) {
        // Check if the user is trying to update their own account
        UUID currentUserId = securityContextUtil.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // If not updating their own account, check if the current user is an admin
        if (!userId.equals(currentUserId) && currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can update other users' accounts");
        }

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(UUID userId) {
        // Check if the user is trying to delete their own account
        UUID currentUserId = securityContextUtil.getCurrentUserId();
        if (!userId.equals(currentUserId)) {
            // If not deleting their own account, check if the current user is an admin
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

            if (currentUser.getRole() != UserRole.ADMIN) {
                throw new RuntimeException("Only admins can delete other users' accounts");
            }
        }

        // Delete all stores owned by the user
        storeService.deleteStoresByOwner(userId);

        // Delete the user
        userRepository.deleteById(userId);
    }

    public User getCurrentUser() {
        try {
            UUID currentUserId = securityContextUtil.getCurrentUserId();
            return userRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Current user not found in database"));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("not authenticated")) {
                throw new RuntimeException("Authentication required. Please login first.");
            }
            throw e;
        }
    }

    public List<User> getPendingUsers() {
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING);
    }

    public List<User> getApprovedUsers() {
        return userRepository.findByApprovalStatus(ApprovalStatus.APPROVED);
    }

    public List<User> getRejectedUsers() {
        return userRepository.findByApprovalStatus(ApprovalStatus.REJECTED);
    }
}