package Commerce360.dto;

import lombok.*;
import Commerce360.entity.AuditLog;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private UUID id;
    private LocalDateTime timestamp;
    private UUID userId;
    private String userEmail;
    private UUID storeId;
    private String storeName;
    private String action;
    private String entityType;
    private UUID entityId;
    private String details;
    private String ipAddress;
    private String userAgent;

    public static AuditLogDTO fromEntity(AuditLog auditLog) {
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .timestamp(auditLog.getTimestamp())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .userEmail(auditLog.getUser() != null ? auditLog.getUser().getEmail() : null)
                .storeId(auditLog.getStore() != null ? auditLog.getStore().getId() : null)
                .storeName(auditLog.getStore() != null ? auditLog.getStore().getName() : null)
                .action(auditLog.getAction())
                .entityType(auditLog.getEntityType())
                .entityId(auditLog.getEntityId())
                .details(auditLog.getDetails())
                .ipAddress(auditLog.getIpAddress())
                .userAgent(auditLog.getUserAgent())
                .build();
    }
}