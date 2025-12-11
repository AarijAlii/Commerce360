package Commerce360.controller;

import Commerce360.entity.Store;
import Commerce360.entity.User;
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
    @Operation(summary = "List All Stores", description = "Get list of all stores with ratings")
    public ResponseEntity<List<StoreDTO>> getAllStores() {
        List<StoreDTO> stores = storeService.getAllStores().stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stores);
    }

    // GET STORE BY ID
    @GetMapping("/{id}")
    public ResponseEntity<StoreDTO> getStoreById(@PathVariable UUID id) {
        return storeService.getStoreById(id)
                .map(StoreDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE A STORE
    @PostMapping
    public ResponseEntity<?> createStore(@RequestBody StoreCreationRequest request) {
        try {
            Store store = Store.builder()
                    .name(request.getName())
                    .location(request.getLocation())
                    .build();

            // If owner is specified in the request, set it
            if (request.getOwner() != null && request.getOwner().getId() != null) {
                User owner = new User();
                owner.setId(request.getOwner().getId());
                store.setOwner(owner);
            }

            Store createdStore = storeService.createStore(store);
            return ResponseEntity.ok(StoreDTO.fromEntity(createdStore));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // UPDATE A STORE
    @PutMapping("/{id}")
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
    public ResponseEntity<List<StoreDTO>> getStoresByOwner(@PathVariable UUID ownerId) {
        List<StoreDTO> stores = storeService.getStoresByOwner(ownerId).stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stores);
    }

    // GET STORES FOR CURRENT USER
    @GetMapping("/my-stores")
    public ResponseEntity<List<StoreDTO>> getStoresForCurrentUser() {
        List<StoreDTO> stores = storeService.getStoresForCurrentUser().stream()
                .map(StoreDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stores);
    }
}

@Data
class StoreCreationRequest {
    private String name;
    private String location;
    private UserDTO owner;
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
