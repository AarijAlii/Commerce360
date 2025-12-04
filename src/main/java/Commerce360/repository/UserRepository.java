package Commerce360.repository;

import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.entity.ApprovalStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    List<User> findByApprovalStatus(ApprovalStatus approvalStatus);

    List<User> findByRole(UserRole role);

    void deleteByEmail(String email);
}
