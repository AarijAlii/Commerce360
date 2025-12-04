package Commerce360.dto;

import Commerce360.entity.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierDTO {
    private UUID id;
    private String name;
    private String contact;
    private String email;
    private String address;

    public static SupplierDTO fromEntity(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contact(supplier.getContact())
                .email(supplier.getEmail())
                .address(supplier.getAddress())
                .build();
    }
}