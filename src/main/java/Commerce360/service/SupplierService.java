package Commerce360.service;

import Commerce360.entity.Supplier;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.SupplierRepository;
import Commerce360.repository.UserRepository;
import Commerce360.security.SecurityContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class SupplierService {
    @Autowired
    private final SupplierRepository supplierRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public SupplierService(SupplierRepository supplierRepository, UserRepository userRepository,
            SecurityContextUtil securityContextUtil) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
        this.securityContextUtil = securityContextUtil;
    }

    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }

    public Optional<Supplier> getSupplierById(UUID id) {
        return supplierRepository.findById(id);
    }

    @Transactional
    public Supplier createSupplier(Supplier supplier) {
        // Check if the current user is an admin
        User currentUser = userRepository.findById(securityContextUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Only admins can create suppliers");
        }

        return supplierRepository.save(supplier);
    }

    @Transactional
    public Optional<Supplier> updateSupplier(UUID id, Supplier supplier) {
        if (!supplierRepository.existsById(id)) {
            return Optional.empty();
        }
        supplier.setId(id);
        return Optional.of(supplierRepository.save(supplier));
    }

    @Transactional
    public boolean deleteSupplier(UUID id) {
        if (!supplierRepository.existsById(id)) {
            return false;
        }
        supplierRepository.deleteById(id);
        return true;
    }

    public Page<Supplier> searchSuppliers(String query, Pageable pageable) {
        return supplierRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query, pageable);
    }
}