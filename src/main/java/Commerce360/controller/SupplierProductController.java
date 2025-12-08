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

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/supplier-products")
public class SupplierProductController {

    @Autowired
    private SupplierProductService supplierProductService;

    @PostMapping
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
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

    @GetMapping("/{id}")
    public ResponseEntity<SupplierProductDTO> getSupplierProduct(@PathVariable UUID id) {
        SupplierProductDTO supplierProduct = supplierProductService.getSupplierProduct(id);
        return ResponseEntity.ok(supplierProduct);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPPLIER') or hasRole('ADMIN')")
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
    public ResponseEntity<Void> deleteSupplierProduct(@PathVariable UUID id) {
        supplierProductService.deleteSupplierProduct(id);
        return ResponseEntity.ok().build();
    }
}
