package Commerce360.dto;

import Commerce360.entity.PurchaseOrder;
import Commerce360.entity.PurchaseOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderDTO {
    private UUID id;
    private String orderNumber;
    private UUID storeId;
    private String storeName;
    private UUID supplierId;
    private String supplierName;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private BigDecimal totalAmount;
    private PurchaseOrderStatus status;
    private String notes;
    private LocalDateTime submittedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private String rejectionReason;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String trackingNumber;
    private String invoiceNumber;

    @Builder.Default
    private List<PurchaseOrderItemDTO> items = new ArrayList<>();

    public static PurchaseOrderDTO fromEntity(PurchaseOrder purchaseOrder) {
        return PurchaseOrderDTO.builder()
                .id(purchaseOrder.getId())
                .orderNumber(purchaseOrder.getOrderNumber())
                .storeId(purchaseOrder.getStore() != null ? purchaseOrder.getStore().getId() : null)
                .storeName(purchaseOrder.getStore() != null ? purchaseOrder.getStore().getName() : null)
                .supplierId(purchaseOrder.getSupplier() != null ? purchaseOrder.getSupplier().getId() : null)
                .supplierName(purchaseOrder.getSupplier() != null ? purchaseOrder.getSupplier().getCompanyName() : null)
                .orderDate(purchaseOrder.getOrderDate())
                .expectedDeliveryDate(purchaseOrder.getExpectedDeliveryDate())
                .totalAmount(purchaseOrder.getTotalAmount())
                .status(purchaseOrder.getStatus())
                .notes(purchaseOrder.getNotes())
                .submittedAt(purchaseOrder.getSubmittedAt())
                .approvedAt(purchaseOrder.getApprovedAt())
                .rejectedAt(purchaseOrder.getRejectedAt())
                .rejectionReason(purchaseOrder.getRejectionReason())
                .shippedAt(purchaseOrder.getShippedAt())
                .deliveredAt(purchaseOrder.getDeliveredAt())
                .cancelledAt(purchaseOrder.getCancelledAt())
                .cancellationReason(purchaseOrder.getCancellationReason())
                .createdAt(purchaseOrder.getCreatedAt())
                .updatedAt(purchaseOrder.getUpdatedAt())
                .trackingNumber(purchaseOrder.getTrackingNumber())
                .invoiceNumber(purchaseOrder.getInvoiceNumber())
                .items(purchaseOrder.getPurchaseOrderItems() != null
                        ? purchaseOrder.getPurchaseOrderItems().stream()
                                .map(PurchaseOrderItemDTO::fromEntity)
                                .collect(Collectors.toList())
                        : new ArrayList<>())
                .build();
    }
}
