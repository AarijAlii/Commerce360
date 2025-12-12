package Commerce360.controller;

import Commerce360.entity.Store;
import Commerce360.service.StoreService;
import Commerce360.dto.StoreDTO;
import Commerce360.dto.UserDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
@Tag(name = "Stores", description = "Store management and ownership assignment")
public class StoreController {
    @Autowired
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping
    @Operation(
        summary = "List All Stores",
        description = "Get list of all stores with ratings (Authenticated users only)"
    )
    public ResponseEntity<List<StoreDTO>> getAllStores() {
        List<StoreDTO> stores = storeService.getAllStores();
        return ResponseEntity.ok(stores);
    }

    // GET STORE BY ID
    @GetMapping("/{id}")
    @Operation(
        summary = "Get Store by ID",
        description = "Get detailed store information including owner and location (Authenticated users only)"
    )
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable UUID id) {
        return storeService.getStoreById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE A STORE
    @PostMapping
    @Operation(
        summary = "Create Store",
        description = "Create a new store. Owner automatically set from current user (STORE_MANAGER or ADMIN only)"
    )
    public ResponseEntity<?> createStore(@RequestBody StoreCreationRequest request) {
        try {
            Store store = Store.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .build();

            // Owner is automatically set by StoreService.createStore() from current user
            
            Store createdStore = storeService.createStore(store);
            return ResponseEntity.ok(StoreDTO.fromEntity(createdStore));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UPDATE A STORE
    @PutMapping("/{id}")
    @Operation(
        summary = "Update Store",
        description = "Update store details (name, location). Must be store owner or ADMIN"
    )
    public ResponseEntity<?> updateStore(@PathVariable UUID id, @RequestBody StoreUpdateRequest request) {
        try {
            Store store = Store.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .build();

            Store updatedStore = storeService.updateStore(id, store);
            return ResponseEntity.ok(StoreDTO.fromEntity(updatedStore));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // DELETE A STORE
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete Store",
        description = "Delete a store. Must be store owner or ADMIN"
    )
    public ResponseEntity<?> deleteStore(@PathVariable UUID id) {
        try {
            storeService.deleteStore(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ASSIGN STORE OWNER
    @PutMapping("/{id}/assign-owner")
    @Operation(
        summary = "Assign Store Owner",
        description = "Transfer store ownership to another user (ADMIN only)"
    )
    public ResponseEntity<StoreDTO> assignStoreOwner(@PathVariable UUID id,
            @RequestBody OwnerAssignmentRequest request) {
        try {
            Store store = storeService.assignStoreOwner(id, request.getUserId());
            return ResponseEntity.ok(StoreDTO.fromEntity(store));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // GET STORES BY OWNER
    @GetMapping("/owned/{ownerId}")
    @Operation(
        summary = "Get Stores by Owner ID",
        description = "Get all stores owned by a specific user/store manager (Authenticated users only)"
    )
    public ResponseEntity<List<StoreDTO>> getStoresByOwner(@PathVariable UUID ownerId) {
        List<StoreDTO> stores = storeService.getStoresByOwner(ownerId);
        return ResponseEntity.ok(stores);
    }

    // GET STORES FOR CURRENT USER
    @GetMapping("/my-stores")
    @Operation(
        summary = "Get My Stores",
        description = "Get all stores owned by the currently logged-in user (STORE_MANAGER only)"
    )
    public ResponseEntity<List<StoreDTO>> getStoresForCurrentUser() {
        List<StoreDTO> stores = storeService.getStoresForCurrentUser();
        return ResponseEntity.ok(stores);
    }
}

@Data
class StoreCreationRequest {
    private String name;
    private String location;
}

@Data
class StoreUpdateRequest {
    private String name;
    private String location;
}

@Data
class OwnerAssignmentRequest {
    private UUID userId;
}
