package Commerce360.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import Commerce360.entity.Transaction;
import Commerce360.entity.Store;
import Commerce360.entity.Product;
import Commerce360.entity.TransactionType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    List<Transaction> findByStore(Store store);

    List<Transaction> findByStoreAndType(Store store, TransactionType type);

    List<Transaction> findByStoreAndTransactionDateBetween(Store store, LocalDateTime startDate, LocalDateTime endDate);

    List<Transaction> findByProduct(Product product);

    Page<Transaction> findByStoreAndTypeAndTransactionDateBetween(
            Store store,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable);
}