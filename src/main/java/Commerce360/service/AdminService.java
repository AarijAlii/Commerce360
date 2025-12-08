package Commerce360.service;

import Commerce360.dto.UserDTO;
import Commerce360.entity.ApprovalStatus;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserService userService;

    // User Management
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getUsersByRole(UserRole role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getUsersByStatus(ApprovalStatus status, Pageable pageable) {
        return userRepository.findByApprovalStatus(status, pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getPendingApprovals(Pageable pageable) {
        return userRepository.findByApprovalStatus(ApprovalStatus.PENDING, pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getPendingSuppliers(Pageable pageable) {
        return userRepository.findByRoleAndApprovalStatus(UserRole.SUPPLIER, ApprovalStatus.PENDING, pageable)
                .map(UserDTO::fromEntity);
    }

    public Page<UserDTO> getPendingStoreManagers(Pageable pageable) {
        return userRepository.findByRoleAndApprovalStatus(UserRole.STORE_MANAGER, ApprovalStatus.PENDING, pageable)
                .map(UserDTO::fromEntity);
    }

    @Transactional
    public UserDTO approveUser(UUID userId) {
        User user = userService.approveUser(userId);
        return UserDTO.fromEntity(user);
    }

    @Transactional
    public UserDTO rejectUser(UUID userId, String reason) {
        User user = userService.rejectUser(userId, reason);
        return UserDTO.fromEntity(user);
    }

    // Platform Analytics
    public Map<String, Object> getPlatformStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // User statistics
        stats.put("totalUsers", userRepository.count());
        stats.put("totalAdmins", userRepository.countByRole(UserRole.ADMIN));
        stats.put("totalStoreManagers", userRepository.countByRole(UserRole.STORE_MANAGER));
        stats.put("totalSuppliers", userRepository.countByRole(UserRole.SUPPLIER));
        stats.put("totalCustomers", userRepository.countByRole(UserRole.CUSTOMER));
        stats.put("pendingApprovals", userRepository.countByApprovalStatus(ApprovalStatus.PENDING));

        // Store statistics
        stats.put("totalStores", storeRepository.count());

        // Product statistics
        stats.put("totalProducts", productRepository.count());

        // Order statistics
        stats.put("totalCustomerOrders", orderRepository.count());
        stats.put("totalPurchaseOrders", purchaseOrderRepository.count());

        // Supplier statistics
        stats.put("totalSuppliers", supplierRepository.count());

        return stats;
    }

    public Map<String, Object> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // By role
        stats.put("byRole", Map.of(
                "ADMIN", userRepository.countByRole(UserRole.ADMIN),
                "STORE_MANAGER", userRepository.countByRole(UserRole.STORE_MANAGER),
                "SUPPLIER", userRepository.countByRole(UserRole.SUPPLIER),
                "CUSTOMER", userRepository.countByRole(UserRole.CUSTOMER)));

        // By approval status
        stats.put("byStatus", Map.of(
                "PENDING", userRepository.countByApprovalStatus(ApprovalStatus.PENDING),
                "APPROVED", userRepository.countByApprovalStatus(ApprovalStatus.APPROVED),
                "REJECTED", userRepository.countByApprovalStatus(ApprovalStatus.REJECTED)));

        // Pending by role
        stats.put("pendingByRole", Map.of(
                "STORE_MANAGER",
                userRepository.countByRoleAndApprovalStatus(UserRole.STORE_MANAGER, ApprovalStatus.PENDING),
                "SUPPLIER", userRepository.countByRoleAndApprovalStatus(UserRole.SUPPLIER, ApprovalStatus.PENDING)));

        return stats;
    }

    public Map<String, Object> getRecentActivity() {
        Map<String, Object> activity = new HashMap<>();

        // Recent registrations (last 7 days)
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        activity.put("recentRegistrations", userRepository.countByRegistrationDateAfter(weekAgo));

        // Recent approvals (last 7 days)
        activity.put("recentApprovals", userRepository.countByApprovalDateAfter(weekAgo));

        return activity;
    }
}
