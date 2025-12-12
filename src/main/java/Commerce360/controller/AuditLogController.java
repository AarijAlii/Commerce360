package Commerce360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import Commerce360.dto.AuditLogDTO;
import Commerce360.service.AuditLogService;

import java.time.LocalDateTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/audit-logs")
@Tag(name = "Audit Logs", description = "Comprehensive audit trail tracking for all system activities")
public class AuditLogController {

    @Autowired
    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get All Audit Logs",
        description = "Retrieve all audit logs with pagination (ADMIN only)"
    )
    public ResponseEntity<Page<AuditLogDTO>> getAllAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAllAuditLogs(pageable));
    }

    @GetMapping("/store/{storeId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(
        summary = "Get Store Audit Logs",
        description = "Get all audit logs for a specific store (STORE_MANAGER only)"
    )
    public ResponseEntity<Page<AuditLogDTO>> getStoreAuditLogs(
            @PathVariable UUID storeId,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getStoreAuditLogs(storeId, pageable));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasRole('STORE_MANAGER')")
    @Operation(
        summary = "Get Product Audit Logs",
        description = "Get all audit logs for specific product activity (STORE_MANAGER only)"
    )
    public ResponseEntity<Page<AuditLogDTO>> getProductAuditLogs(
            @PathVariable UUID productId,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getProductAuditLogs(productId, pageable));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get Audit Logs by Date Range",
        description = "Filter audit logs by date range for compliance reporting (ADMIN only)"
    )
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByDateRange(startDate, endDate, pageable));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get User Activity Logs",
        description = "Get all activity logs for a specific user (ADMIN only)"
    )
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogsByUser(
            @PathVariable UUID userId,
            Pageable pageable) {
        return ResponseEntity.ok(auditLogService.getAuditLogsByUser(userId, pageable));
    }
}



