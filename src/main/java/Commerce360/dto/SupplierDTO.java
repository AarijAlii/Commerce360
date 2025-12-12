package Commerce360.dto;

import Commerce360.entity.ApprovalStatus;
import Commerce360.entity.Supplier;
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
public class SupplierDTO {
    private UUID id;
    private UUID userId;
    private String companyName;
    private String businessLicense;
    private String taxId;
    private String description;
    private String contact;
    private String email;
    private String address;
    private String city;
    private String country;
    private ApprovalStatus approvalStatus;
    private LocalDateTime registrationDate;
    private LocalDateTime approvalDate;
    private String rejectionReason;
    private Boolean isActive;
    private Double rating;
    private Integer totalOrders;

    public static SupplierDTO fromEntity(Supplier supplier) {
        return SupplierDTO.builder()
                .id(supplier.getId())
                .userId(supplier.getUser() != null ? supplier.getUser().getId() : null)
                .companyName(supplier.getCompanyName())
                .businessLicense(supplier.getBusinessLicense())
                .taxId(supplier.getTaxId())
                .description(supplier.getDescription())
                .contact(supplier.getContact())
                .email(supplier.getUser() != null ? supplier.getUser().getEmail() : null)
                .address(supplier.getAddress())
                .city(supplier.getCity())
                .country(supplier.getCountry())
                .approvalStatus(supplier.getUser() != null ? supplier.getUser().getApprovalStatus() : null)
                .registrationDate(supplier.getUser() != null ? supplier.getUser().getRegistrationDate() : null)
                .approvalDate(supplier.getUser() != null ? supplier.getUser().getApprovalDate() : null)
                .rejectionReason(supplier.getUser() != null ? supplier.getUser().getRejectionReason() : null)
                .isActive(supplier.getIsActive())
                .rating(supplier.getRating())
                .totalOrders(supplier.getTotalOrders())
                .build();
    }
}