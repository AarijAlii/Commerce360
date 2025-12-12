package Commerce360.service;

import Commerce360.dto.StoreDTO;
import Commerce360.entity.Store;
import Commerce360.entity.StoreManager;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
import Commerce360.repository.StoreManagerRepository;
import Commerce360.repository.StoreRepository;
import Commerce360.repository.UserRepository;
import Commerce360.security.SecurityContextUtil;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.cache.annotation.CacheEvict;
// import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StoreService {
    @Autowired
    private final StoreRepository storeRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final StoreManagerRepository storeManagerRepository;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public StoreService(StoreRepository storeRepository, UserRepository userRepository,
            StoreManagerRepository storeManagerRepository, SecurityContextUtil securityContextUtil) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.storeManagerRepository = storeManagerRepository;
        this.securityContextUtil = securityContextUtil;
    }

    // @Cacheable(value = "stores", key = "'all'")
    @Transactional(readOnly = true)
    public List<StoreDTO> getAllStores() {
        return storeRepository.findAll().stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // @Cacheable(value = "stores", key = "#id")
    @Transactional(readOnly = true)
    public Optional<StoreDTO> getStoreById(UUID id) {
        return storeRepository.findById(id)
                .map(StoreDTO::fromEntity);
    }

    // @CacheEvict(value = "stores", allEntries = true)
    public Store createStore(Store store) {
        // Get or create StoreManager for current user
        User currentUser = userRepository.findById(securityContextUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (currentUser.getRole() != UserRole.STORE_MANAGER) {
            throw new RuntimeException("Only users with STORE_MANAGER role can create stores");
        }

        // Get or create StoreManager entity
        StoreManager storeManager = storeManagerRepository.findByUserId(currentUser.getId())
                .orElseGet(() -> {
                    StoreManager newManager = StoreManager.builder()
                            .user(currentUser)
                            .build();
                    return storeManagerRepository.save(newManager);
                });

        store.setOwner(storeManager);
        return storeRepository.save(store);
    }

    // @CacheEvict(value = "stores", key = "#storeId")
    public Store updateStore(UUID storeId, Store updatedStore) {
        Store existingStore = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Check if the current user is the owner of the store
        UUID currentUserId = securityContextUtil.getCurrentUserId();
        if (!existingStore.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("Only the store owner can update the store");
        }

        existingStore.setName(updatedStore.getName());
        existingStore.setLocation(updatedStore.getLocation());

        return storeRepository.save(existingStore);
    }

    // @CacheEvict(value = "stores", key = "#storeId")
    public void deleteStore(UUID storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        // Check if the current user is the owner of the store
        UUID currentUserId = securityContextUtil.getCurrentUserId();
        if (!store.getOwner().getId().equals(currentUserId)) {
            throw new RuntimeException("Only the store owner can delete the store");
        }

        storeRepository.deleteById(storeId);
    }

    @Transactional
    // @CacheEvict(value = "stores", allEntries = true)
    public void deleteStoresByOwner(UUID ownerId) {
        List<Store> stores = storeRepository.findByOwnerId(ownerId);
        for (Store store : stores) {
            storeRepository.delete(store);
        }
    }

    // @CacheEvict(value = "stores", key = "#storeId")
    public Store assignStoreOwner(UUID storeId, UUID userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != UserRole.STORE_MANAGER) {
            throw new RuntimeException("Store owner must have STORE_MANAGER role");
        }

        // Get or create StoreManager
        StoreManager storeManager = storeManagerRepository.findByUserId(userId)
                .orElseGet(() -> {
                    StoreManager newManager = StoreManager.builder()
                            .user(user)
                            .build();
                    return storeManagerRepository.save(newManager);
                });

        store.setOwner(storeManager);
        return storeRepository.save(store);
    }

    // @Cacheable(value = "stores", key = "'owner_' + #ownerId")
    @Transactional(readOnly = true)
    public List<StoreDTO> getStoresByOwner(UUID ownerId) {
        return storeRepository.findByOwnerId(ownerId).stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Note: ownerId is now StoreManager.id, not User.id
    // To get stores for a user, first get their StoreManager
    @Transactional(readOnly = true)
    public List<StoreDTO> getStoresForCurrentUser() {
        User currentUser = userRepository.findById(securityContextUtil.getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        StoreManager storeManager = storeManagerRepository.findByUserId(currentUser.getId())
                .orElse(null);

        if (storeManager == null) {
            return List.of(); // No stores if no StoreManager entity
        }

        return getStoresByOwner(storeManager.getId());
    }
}
