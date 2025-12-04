package Commerce360.dto;

import lombok.*;
import Commerce360.entity.Transaction;
import Commerce360.entity.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private UUID id;
    private UUID storeId;
    private String storeName;
    private UUID productId;
    private String productName;
    private TransactionType type;
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
    private LocalDateTime transactionDate;
    private LocalDateTime expiryDate;
    private String batchNumber;
    private String notes;
    private UUID supplierId;
    private String supplierName;
    private String referenceNumber;

    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .id(transaction.getId())
                .storeId(transaction.getStore().getId())
                .storeName(transaction.getStore().getName())
                .productId(transaction.getProduct().getId())
                .productName(transaction.getProduct().getName())
                .type(transaction.getType())
                .quantity(transaction.getQuantity())
                .unitPrice(transaction.getUnitPrice())
                .totalAmount(transaction.getTotalAmount())
                .transactionDate(transaction.getTransactionDate())
                .expiryDate(transaction.getExpiryDate())
                .batchNumber(transaction.getBatchNumber())
                .notes(transaction.getNotes())
                .supplierId(transaction.getSupplier() != null ? transaction.getSupplier().getId() : null)
                .supplierName(transaction.getSupplier() != null ? transaction.getSupplier().getName() : null)
                .referenceNumber(transaction.getReferenceNumber())
                .build();
    }
}