package Commerce360.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Commerce360.entity.AuditLog;
import Commerce360.entity.Store;
import Commerce360.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID>, JpaSpecificationExecutor<AuditLog> {
    List<AuditLog> findByStore(Store store);

    List<AuditLog> findByUser(User user);

    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<AuditLog> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    Page<AuditLog> findByStoreAndTimestampBetween(
            Store store, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}