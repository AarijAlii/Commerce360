package Commerce360.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import Commerce360.dto.AuditLogDTO;
import Commerce360.entity.AuditLog;
import Commerce360.entity.Store;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.AuditLogRepository;
import Commerce360.security.SecurityContextUtil;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditLogService {

    @Autowired
    private final AuditLogRepository auditLogRepository;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public AuditLogService(AuditLogRepository auditLogRepository, SecurityContextUtil securityContextUtil) {
        this.auditLogRepository = auditLogRepository;
        this.securityContextUtil = securityContextUtil;
    }

    @Transactional
    public void logAction(String action, String entityType, UUID entityId, String details) {
        User currentUser = securityContextUtil.getCurrentUser()
                .orElseThrow(() -> new RuntimeException("No authenticated user found"));

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ipAddress = attributes != null ? attributes.getRequest().getRemoteAddr() : "unknown";
        String userAgent = attributes != null ? attributes.getRequest().getHeader("User-Agent") : "unknown";

        Store userStore = null;
        if (currentUser.getRole() == UserRole.STORE_MANAGER && !currentUser.getManagedStores().isEmpty()) {
            userStore = currentUser.getManagedStores().get(0);
        }

        AuditLog auditLog = AuditLog.builder()
                .timestamp(LocalDateTime.now())
                .user(currentUser)
                .store(userStore)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
    }
@Transactional
public void logAction(User user, Store store, String action, String entityType, UUID entityId, String details) {
    ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    String ipAddress = attributes != null ? attributes.getRequest().getRemoteAddr() : "unknown";
    String userAgent = attributes != null ? attributes.getRequest().getHeader("User-Agent") : "unknown";
    AuditLog auditLog = AuditLog.builder()
            .timestamp(LocalDateTime.now())
            .user(user)
            .store(store)
            .action(action)
            .entityType(entityType)
            .entityId(entityId)
            .details(details)
            .ipAddress(ipAddress)
            .userAgent(userAgent)
            .build();
    auditLogRepository.save(auditLog);
}
    public Page<AuditLogDTO> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> getStoreAuditLogs(UUID storeId, Pageable pageable) {
        return auditLogRepository.findAll(
                (Specification<AuditLog>) (root, query, cb) -> cb.equal(root.get("store").get("id"), storeId),
                pageable)
                .map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> getProductAuditLogs(UUID productId, Pageable pageable) {
        return auditLogRepository.findAll(
                (Specification<AuditLog>) (root, query, cb) -> cb.and(
                        cb.equal(root.get("entityType"), "PRODUCT"),
                        cb.equal(root.get("entityId"), productId)),
                pageable)
                .map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate,
            Pageable pageable) {
        return auditLogRepository.findAll(
                (Specification<AuditLog>) (root, query, cb) -> cb.between(root.get("timestamp"), startDate, endDate),
                pageable)
                .map(AuditLogDTO::fromEntity);
    }

    public Page<AuditLogDTO> getAuditLogsByUser(UUID userId, Pageable pageable) {
        return auditLogRepository.findAll(
                (Specification<AuditLog>) (root, query, cb) -> cb.equal(root.get("user").get("id"), userId),
                pageable)
                .map(AuditLogDTO::fromEntity);
    }
}