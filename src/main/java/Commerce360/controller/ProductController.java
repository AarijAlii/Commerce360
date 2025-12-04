package Commerce360.controller;

import Commerce360.entity.Product;
import Commerce360.service.ProductService;
import Commerce360.service.RateLimiterService;
import Commerce360.dto.ProductDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

//PRODUCT CONTROLLER GLOBAL PRODUCT CATALOG
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private final ProductService productService;
    @Autowired
    private final RateLimiterService rateLimiterService;

    public ProductController(ProductService productService, RateLimiterService rateLimiterService) {
        this.productService = productService;
        this.rateLimiterService = rateLimiterService;
    }

    // Test endpoint for rate limiter metrics
    @GetMapping("/test-metrics")
    public ResponseEntity<String> testMetrics(@RequestParam(defaultValue = "test-client") String clientId) {
        try {
            rateLimiterService.tryAcquire(clientId);
            return ResponseEntity.ok("Request allowed");
        } catch (Exception e) {
            return ResponseEntity.status(429).body("Rate limit exceeded");
        }
    }

    // GET ALL PRODUCTS
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getAllProducts(pageRequest);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(productDTOs);
    }

    // GET PRODUCTS BY CATEGORY
    @GetMapping("/category/{category}")
    public ResponseEntity<Page<ProductDTO>> getProductsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getProductsByCategory(category, pageRequest);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(productDTOs);
    }

    // SEARCH PRODUCTS
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDTO>> searchProducts(
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

        Page<Product> products = productService.searchProducts(query, pageRequest);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(productDTOs);
    }

    // GET PRODUCTS BY SUPPLIER
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Page<ProductDTO>> getProductsBySupplier(
            @PathVariable UUID supplierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getProductsBySupplier(supplierId, pageRequest);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(productDTOs);
    }

    // DYNAMIC FILTERED PRODUCTS
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDTO>> getFilteredProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {

        Sort.Direction direction = sortDirection != null && sortDirection.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = sortBy != null ? Sort.by(direction, sortBy) : Sort.unsorted();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Product> products = productService.getProductsWithFilters(category, supplierId, searchQuery, pageRequest);
        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);

        return ResponseEntity.ok(productDTOs);
    }

    // GET PRODUCT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable UUID id) {
        return productService.getProductById(id)
                .map(ProductDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE A PRODUCT
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.ok(ProductDTO.fromEntity(createdProduct));
    }

    // UPDATE A PRODUCT
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable UUID id, @RequestBody Product product) {
        return productService.updateProduct(id, product)
                .map(ProductDTO::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE A PRODUCT
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
