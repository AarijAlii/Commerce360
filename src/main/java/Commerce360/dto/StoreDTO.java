package Commerce360.dto;

import Commerce360.entity.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDTO {
    private UUID id;
    private String name;
    private String location;
    private UserDTO owner;

    public static StoreDTO fromEntity(Store store) {
        if (store == null) {
            return null;
        }

        return StoreDTO.builder()
                .id(store.getId())
                .name(store.getName())
                .location(store.getLocation())
                .owner(store.getOwner() != null && store.getOwner().getUser() != null
                        ? UserDTO.fromEntity(store.getOwner().getUser())
                        : null)
                .build();
    }
}