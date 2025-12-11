package Commerce360.repository;

import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.entity.ApprovalStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByApprovalStatus(ApprovalStatus approvalStatus);

    Page<User> findByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    List<User> findByRole(UserRole role);

    Page<User> findByRole(UserRole role, Pageable pageable);

    Page<User> findByRoleAndApprovalStatus(UserRole role, ApprovalStatus approvalStatus, Pageable pageable);

    void deleteByEmail(String email);

    // Count methods for statistics
    Long countByRole(UserRole role);

    Long countByApprovalStatus(ApprovalStatus approvalStatus);

    Long countByRoleAndApprovalStatus(UserRole role, ApprovalStatus approvalStatus);

    Long countByRegistrationDateAfter(LocalDateTime date);

    Long countByApprovalDateAfter(LocalDateTime date);
}
