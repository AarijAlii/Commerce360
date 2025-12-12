package Commerce360.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import Commerce360.entity.Supplier;
import Commerce360.entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    // Search by company name or user email
    Page<Supplier> findByCompanyNameContainingIgnoreCaseOrUser_EmailContainingIgnoreCase(
            String companyName, String email, Pageable pageable);

    Optional<Supplier> findByUser(User user);

    Optional<Supplier> findByUser_Id(UUID userId);
}