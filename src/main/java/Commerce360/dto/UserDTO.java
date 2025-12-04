package Commerce360.dto;

import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String email;
    private UserRole role;
    private ApprovalStatus approvalStatus;
    private LocalDateTime registrationDate;
    private LocalDateTime approvalDate;
    private String rejectionReason;

    public static UserDTO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .approvalStatus(user.getApprovalStatus())
                .registrationDate(user.getRegistrationDate())
                .approvalDate(user.getApprovalDate())
                .rejectionReason(user.getRejectionReason())
                .build();
    }
}