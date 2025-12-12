package Commerce360.controller;

import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.service.UserService;
import Commerce360.dto.UserDTO;
import Commerce360.security.SecurityContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User registration, profile, and approval management")
public class UserController {
    @Autowired
    private final UserService userService;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public UserController(UserService userService, SecurityContextUtil securityContextUtil) {
        this.userService = userService;
        this.securityContextUtil = securityContextUtil;
    }

    @GetMapping("/me")
    @Operation(
        summary = "Get Current User Profile",
        description = "Get currently logged-in user's profile information"
    )
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = userService.getCurrentUser();
            return ResponseEntity.ok(UserDTO.fromEntity(user));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Authentication required")) {
                return ResponseEntity.status(401).body("Authentication required. Please login first.");
            }
            return ResponseEntity.status(500).body("An error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register User", description = "Register supplier or store manager. Status: PENDING (requires admin approval). For customers use /api/customers/register.")
    @SecurityRequirement(name = "")
    public ResponseEntity<UserDTO> registerUser(
            @Parameter(description = "Registration details") @RequestBody RegistrationRequest request) {
        User registeredUser = userService.registerUser(request.getEmail(), request.getPassword(), request.getRole(),
                request.getFirstName(), request.getLastName());
        return ResponseEntity.ok(UserDTO.fromEntity(registeredUser));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get Pending Users",
        description = "Get all users awaiting approval (ADMIN only)"
    )
    public ResponseEntity<List<UserDTO>> getPendingUsers() {
        List<User> pendingUsers = userService.getPendingUsers();
        List<UserDTO> pendingUserDTOs = pendingUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(pendingUserDTOs);
    }

    @GetMapping("/approved")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get Approved Users",
        description = "Get all approved users (ADMIN only)"
    )
    public ResponseEntity<List<UserDTO>> getApprovedUsers() {
        List<User> approvedUsers = userService.getApprovedUsers();
        List<UserDTO> approvedUserDTOs = approvedUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(approvedUserDTOs);
    }

    @GetMapping("/rejected")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get Rejected Users",
        description = "Get all rejected user registrations (ADMIN only)"
    )
    public ResponseEntity<List<UserDTO>> getRejectedUsers() {
        List<User> rejectedUsers = userService.getRejectedUsers();
        List<UserDTO> rejectedUserDTOs = rejectedUsers.stream()
                .map(UserDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rejectedUserDTOs);
    }

    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Approve User",
        description = "Approve pending user registration (ADMIN only)"
    )
    public ResponseEntity<UserDTO> approveUser(@PathVariable UUID userId) {
        User approvedUser = userService.approveUser(userId);
        return ResponseEntity.ok(UserDTO.fromEntity(approvedUser));
    }

    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Reject User",
        description = "Reject user registration with reason (ADMIN only)"
    )
    public ResponseEntity<UserDTO> rejectUser(@PathVariable UUID userId, @RequestBody String reason) {
        User rejectedUser = userService.rejectUser(userId, reason);
        return ResponseEntity.ok(UserDTO.fromEntity(rejectedUser));
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update User",
        description = "Update user information"
    )
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID userId, @RequestBody User updatedUser) {
        User user = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(UserDTO.fromEntity(user));
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete User",
        description = "Delete user account"
    )
    public ResponseEntity<Void> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // DELETE CURRENT USER'S ACCOUNT
    @DeleteMapping("/me")
    @Operation(
        summary = "Delete Own Account",
        description = "Delete currently logged-in user's account"
    )
    public ResponseEntity<?> deleteCurrentUser() {
        try {
            userService.deleteUser(securityContextUtil.getCurrentUserId());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

// REGISTRATION REQUEST
@Data
class RegistrationRequest {
    private String email;
    private String password;
    private UserRole role;
    private String firstName;
    private String lastName;
}

// REJECTION REQUEST
@Data
class RejectionRequest {
    private String reason;
}
