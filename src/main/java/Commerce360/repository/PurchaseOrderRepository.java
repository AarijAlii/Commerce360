package Commerce360.repository;

import Commerce360.entity.PurchaseOrder;
import Commerce360.entity.PurchaseOrderStatus;
import Commerce360.entity.Store;
import Commerce360.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    Page<PurchaseOrder> findByStore(Store store, Pageable pageable);

    Page<PurchaseOrder> findBySupplier(Supplier supplier, Pageable pageable);

    Page<PurchaseOrder> findByStoreAndStatus(Store store, PurchaseOrderStatus status, Pageable pageable);

    Page<PurchaseOrder> findBySupplierAndStatus(Supplier supplier, PurchaseOrderStatus status, Pageable pageable);

    @Query("SELECT po FROM PurchaseOrder po WHERE po.store = :store AND po.orderDate BETWEEN :startDate AND :endDate")
    List<PurchaseOrder> findByStoreAndDateRange(@Param("store") Store store,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    Long countByStoreAndStatus(Store store, PurchaseOrderStatus status);

    Long countBySupplierAndStatus(Supplier supplier, PurchaseOrderStatus status);
}
