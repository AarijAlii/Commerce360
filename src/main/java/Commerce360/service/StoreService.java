package Commerce360.service;

import Commerce360.entity.Store;
import Commerce360.entity.User;
import Commerce360.entity.UserRole;
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

@Service
public class StoreService {
    @Autowired
    private final StoreRepository storeRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final SecurityContextUtil securityContextUtil;

    public StoreService(StoreRepository storeRepository, UserRepository userRepository,
            SecurityContextUtil securityContextUtil) {
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
        this.securityContextUtil = securityContextUtil;
    }

    // @Cacheable(value = "stores", key = "'all'")
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // @Cacheable(value = "stores", key = "#id")
    public Optional<Store> getStoreById(UUID id) {
        return storeRepository.findById(id);
    }

    // @CacheEvict(value = "stores", allEntries = true)
    public Store createStore(Store store) {
        // If no owner is specified, use the current user
        if (store.getOwner() == null) {
            User currentUser = userRepository.findById(securityContextUtil.getCurrentUserId())
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

            if (currentUser.getRole() != UserRole.STORE_MANAGER) {
                throw new RuntimeException("Only users with STORE_MANAGER role can create stores");
            }

            store.setOwner(currentUser);
        } else {
            // Validate that the specified owner exists and is a STORE_MANAGER
            User owner = userRepository.findById(store.getOwner().getId())
                    .orElseThrow(() -> new RuntimeException("Store owner not found"));

            if (owner.getRole() != UserRole.STORE_MANAGER) {
                throw new RuntimeException("Store owner must have STORE_MANAGER role");
            }
        }

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

        store.setOwner(user);
        return storeRepository.save(store);
    }

    // @Cacheable(value = "stores", key = "'owner_' + #ownerId")
    public List<Store> getStoresByOwner(UUID ownerId) {
        return storeRepository.findByOwnerId(ownerId);
    }

    // @Cacheable(value = "stores", key = "'current_user_' +
    // #securityContextUtil.getCurrentUserId()")
    public List<Store> getStoresForCurrentUser() {
        return getStoresByOwner(securityContextUtil.getCurrentUserId());
    }
}
