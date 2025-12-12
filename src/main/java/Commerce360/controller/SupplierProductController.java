package Commerce360.controller;

import Commerce360.dto.SupplierProductDTO;
import Commerce360.service.SupplierProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplier-products")
@Tag(name = "Supplier Products (B2B)", description = "Supplier product catalog for B2B procurement")
public class SupplierProductController {

    @Autowired
    private SupplierProductService supplierProductService;

    @PostMapping
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
    @Operation(summary = "Add Product to Supplier Catalog", description = "Supplier adds a product to their catalog with pricing and inventory (SUPPLIER or ADMIN only)")
    public ResponseEntity<SupplierProductDTO> addProductToCatalog(
            @RequestParam UUID supplierId,
            @RequestParam UUID productId,
            @RequestParam String supplierSku,
            @RequestParam BigDecimal supplierPrice,
            @RequestParam(required = false) Integer minimumOrderQuantity,
            @RequestParam(required = false) Integer stockAvailable,
            @RequestParam(required = false) Integer leadTimeDays,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl) {

        SupplierProductDTO supplierProduct = supplierProductService.addProductToSupplierCatalog(
                supplierId, productId, supplierSku, supplierPrice,
                minimumOrderQuantity, stockAvailable, leadTimeDays, description, imageUrl);
        return ResponseEntity.ok(supplierProduct);
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get Supplier's Product Catalog", description = "Get all products offered by a specific supplier (Public access)")
    public ResponseEntity<Page<SupplierProductDTO>> getSupplierProducts(
            @PathVariable UUID supplierId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<SupplierProductDTO> products = supplierProductService.getSupplierProducts(
                supplierId, activeOnly, pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Operation(
        summary = "Browse Available Supplier Products",
        description = "Browse all active products available from suppliers for B2B procurement (Public access)"
    )
    public ResponseEntity<Page<SupplierProductDTO>> getAllAvailableProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<SupplierProductDTO> products = supplierProductService.getAllAvailableProducts(pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/my-products")
    @PreAuthorize("hasRole('SUPPLIER')")
    @Operation(
        summary = "Get My Supplier Products",
        description = "Get all products in current supplier's catalog (SUPPLIER only)"
    )
    public ResponseEntity<Page<SupplierProductDTO>> getMyProducts(
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<SupplierProductDTO> products = supplierProductService.getMyProducts(activeOnly, pageRequest);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Supplier Product Details", description = "Get detailed information about a specific supplier product (Public access)")
    public ResponseEntity<SupplierProductDTO> getSupplierProduct(@PathVariable UUID id) {
        SupplierProductDTO supplierProduct = supplierProductService.getSupplierProduct(id);
        return ResponseEntity.ok(supplierProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
    @Operation(summary = "Update Supplier Product", description = "Update pricing, stock, or other details of a supplier product (SUPPLIER or ADMIN only)")
    public ResponseEntity<SupplierProductDTO> updateSupplierProduct(
            @PathVariable UUID id,
            @RequestParam(required = false) BigDecimal supplierPrice,
            @RequestParam(required = false) Integer stockAvailable,
            @RequestParam(required = false) Integer leadTimeDays,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String imageUrl) {

        SupplierProductDTO supplierProduct = supplierProductService.updateSupplierProduct(
                id, supplierPrice, stockAvailable, leadTimeDays, isActive, description, imageUrl);
        return ResponseEntity.ok(supplierProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
    @Operation(summary = "Delete Supplier Product", description = "Remove a product from supplier's catalog (SUPPLIER or ADMIN only)")
    public ResponseEntity<Void> deleteSupplierProduct(@PathVariable UUID id) {
        supplierProductService.deleteSupplierProduct(id);
        return ResponseEntity.ok().build();
    }
}
