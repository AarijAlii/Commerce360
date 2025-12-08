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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ========== User Management ==========

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

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
    public ResponseEntity<UserDTO> approveUser(@PathVariable UUID userId) {
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
