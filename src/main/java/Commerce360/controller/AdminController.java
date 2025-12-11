package Commerce360.controller;

import Commerce360.dto.UserDTO;
import Commerce360.entity.ApprovalStatus;
import Commerce360.entity.UserRole;
import Commerce360.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Panel", description = "Admin-only endpoints for user management, approvals, and platform analytics")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ========== User Management ==========

    @GetMapping("/users")
    @Operation(summary = "List All Users", description = "Get paginated list of all users with sorting options")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "registrationDate") String sortBy,
            @Parameter(description = "Sort direction (ASC/DESC)") @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/role/{role}")
    public ResponseEntity<Page<UserDTO>> getUsersByRole(
            @PathVariable UserRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> users = adminService.getUsersByRole(role, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/status/{status}")
    public ResponseEntity<Page<UserDTO>> getUsersByStatus(
            @PathVariable ApprovalStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> users = adminService.getUsersByStatus(status, pageable);
        return ResponseEntity.ok(users);
    }

    // ========== Approval Workflows ==========

    @GetMapping("/approvals/pending")
    public ResponseEntity<Page<UserDTO>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserDTO> users = adminService.getPendingApprovals(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/approvals/pending/suppliers")
    public ResponseEntity<Page<UserDTO>> getPendingSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").ascending());
        Page<UserDTO> users = adminService.getPendingSuppliers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/approvals/pending/store-managers")
    public ResponseEntity<Page<UserDTO>> getPendingStoreManagers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").ascending());
        Page<UserDTO> users = adminService.getPendingStoreManagers(pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/approve")
    @Operation(summary = "Approve User", description = "Approve a pending supplier or store manager. Tracks who approved and when.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User approved successfully"),
            @ApiResponse(responseCode = "400", description = "User already approved or cannot be approved"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserDTO> approveUser(
            @Parameter(description = "User ID to approve") @PathVariable UUID userId) {
        UserDTO user = adminService.approveUser(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{userId}/reject")
    public ResponseEntity<UserDTO> rejectUser(
            @PathVariable UUID userId,
            @RequestParam String reason) {

        UserDTO user = adminService.rejectUser(userId, reason);
        return ResponseEntity.ok(user);
    }

    // ========== Platform Analytics ==========

    @GetMapping("/statistics/platform")
    @Operation(summary = "Get Platform Statistics", description = "Get comprehensive platform-wide statistics including user counts, orders, products, and stores")
    public ResponseEntity<Map<String, Object>> getPlatformStatistics() {
        Map<String, Object> stats = adminService.getPlatformStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/users")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = adminService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/activity")
    public ResponseEntity<Map<String, Object>> getRecentActivity() {
        Map<String, Object> activity = adminService.getRecentActivity();
        return ResponseEntity.ok(activity);
    }
}
