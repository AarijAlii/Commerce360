package Commerce360.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Commerce360.entity.Supplier;

import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Page<Supplier> findByCompanyNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email,
            Pageable pageable);
}