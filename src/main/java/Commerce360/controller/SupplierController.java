package Commerce360.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import Commerce360.entity.Supplier;
import Commerce360.service.SupplierService;
import Commerce360.dto.SupplierDTO;
import Commerce360.dto.SupplierRegistrationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier profile management and directory")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @PostMapping("/register")
    @Operation(summary = "Register Supplier", description = "Register new supplier with complete business details. **Requires admin approval** before supplier can sell.")
    @SecurityRequirement(name = "")
    public ResponseEntity<SupplierDTO> registerSupplier(
            @Valid @RequestBody SupplierRegistrationRequest request) {

        SupplierDTO supplier = supplierService.registerSupplier(
                request.getEmail(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getCompanyName(),
                request.getBusinessLicense(),
                request.getTaxId(),
                request.getDescription(),
                request.getContact(),
                request.getAddress(),
                request.getCity(),
                request.getCountry());

        return ResponseEntity.ok(supplier);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('SUPPLIER')")
    @Operation(summary = "Get Own Supplier Profile", description = "Get current supplier's profile (SUPPLIER only)")
    public ResponseEntity<SupplierDTO> getMyProfile() {
        SupplierDTO supplier = supplierService.getCurrentSupplierProfile();
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('SUPPLIER')")
    @Operation(summary = "Update Own Supplier Profile", description = "Supplier can update their own business details (SUPPLIER only)")
    public ResponseEntity<SupplierDTO> updateMyProfile(
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) String businessLicense,
            @RequestParam(required = false) String taxId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String contact,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country) {

        SupplierDTO supplier = supplierService.updateOwnProfile(
                companyName, businessLicense, taxId, description,
                contact, address, city, country);
        return ResponseEntity.ok(supplier);
    }

    @GetMapping
    @Operation(summary = "Get All Suppliers", description = "Get paginated list of all suppliers (Authenticated users only)")
    public ResponseEntity<Page<SupplierDTO>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Supplier> suppliers = supplierService.getAllSuppliers(pageRequest);
        Page<SupplierDTO> supplierDTOs = suppliers.map(SupplierDTO::fromEntity);

        return ResponseEntity.ok(supplierDTOs);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Supplier by ID", description = "Get detailed supplier information by ID (Authenticated users only)")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable UUID id) {
        return supplierService.getSupplierById(id)
                .map(SupplierDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create Supplier (Admin)", description = "Manually create a supplier (ADMIN only)")
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody Supplier supplier) {
        Supplier createdSupplier = supplierService.createSupplier(supplier);
        return ResponseEntity.ok(SupplierDTO.fromEntity(createdSupplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Supplier by ID (Admin)", description = "Update supplier information by ID (ADMIN only)")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @RequestBody Supplier supplier) {
        return supplierService.updateSupplier(id, supplier)
                .map(SupplierDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Supplier (Admin)", description = "Delete supplier by ID (ADMIN only)")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        if (supplierService.deleteSupplier(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search Suppliers", description = "Search suppliers by company name or email (Authenticated users only)")
    public ResponseEntity<Page<SupplierDTO>> searchSuppliers(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Supplier> suppliers = supplierService.searchSuppliers(query, pageRequest);
        Page<SupplierDTO> supplierDTOs = suppliers.map(SupplierDTO::fromEntity);

        return ResponseEntity.ok(supplierDTOs);
    }
}