package Commerce360.controller;

import Commerce360.dto.StoreManagerDTO;
import Commerce360.dto.StoreManagerRegistrationRequest;
import Commerce360.service.StoreManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/store-managers")
@RequiredArgsConstructor
@Tag(name = "Store Managers", description = "Store manager registration and management")
public class StoreManagerController {

    private final StoreManagerService storeManagerService;

    @PostMapping("/register")
    @Operation(summary = "Register Store Manager", description = "Register new store manager. **Requires admin approval** before manager can create stores.")
    @SecurityRequirement(name = "")
    public ResponseEntity<StoreManagerDTO> registerStoreManager(
            @Valid @RequestBody StoreManagerRegistrationRequest request) {

        StoreManagerDTO manager = storeManagerService.registerStoreManager(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber());

        return ResponseEntity.ok(manager);
    }
}
