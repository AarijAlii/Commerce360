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

import java.util.UUID;

@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Supplier profile management and directory")
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @GetMapping
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
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable UUID id) {
        return supplierService.getSupplierById(id)
                .map(SupplierDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierDTO> createSupplier(@RequestBody Supplier supplier) {
        Supplier createdSupplier = supplierService.createSupplier(supplier);
        return ResponseEntity.ok(SupplierDTO.fromEntity(createdSupplier));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable UUID id, @RequestBody Supplier supplier) {
        return supplierService.updateSupplier(id, supplier)
                .map(SupplierDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        if (supplierService.deleteSupplier(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
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