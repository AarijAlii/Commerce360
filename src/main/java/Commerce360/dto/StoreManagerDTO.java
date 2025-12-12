package Commerce360.dto;

import Commerce360.entity.StoreManager;
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
public class StoreManagerDTO {
    private UUID id;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static StoreManagerDTO fromEntity(StoreManager manager) {
        if (manager == null) {
            return null;
        }

        return StoreManagerDTO.builder()
                .id(manager.getId())
                .userId(manager.getUser() != null ? manager.getUser().getId() : null)
                .email(manager.getUser() != null ? manager.getUser().getEmail() : null)
                .firstName(manager.getUser() != null ? manager.getUser().getFirstName() : null)
                .lastName(manager.getUser() != null ? manager.getUser().getLastName() : null)
                .phoneNumber(manager.getPhoneNumber())
                .isActive(manager.getIsActive())
                .createdAt(manager.getCreatedAt())
                .build();
    }
}
