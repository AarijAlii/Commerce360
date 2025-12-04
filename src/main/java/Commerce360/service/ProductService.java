package Commerce360.service;

import Commerce360.entity.Product;
import Commerce360.entity.Supplier;
import Commerce360.repository.ProductRepository;
import Commerce360.repository.SupplierRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductService {
    @Autowired
    private final ProductRepository productRepository;

    @Autowired
    private final SupplierRepository supplierRepository;

    public ProductService(ProductRepository productRepository, SupplierRepository supplierRepository) {
        this.productRepository = productRepository;
        this.supplierRepository = supplierRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getProductsByCategory(String category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> searchProducts(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(query, query, pageable);
    }

    public Page<Product> getProductsBySupplier(UUID supplierId, Pageable pageable) {
        Optional<Supplier> supplier = supplierRepository.findById(supplierId);
        if (supplier.isEmpty()) {
            return Page.empty(pageable);
        }
        return productRepository.findBySupplier(supplier.get(), pageable);
    }

    public Page<Product> getProductsWithFilters(String category, UUID supplierId, String searchQuery,
            Pageable pageable) {
        Specification<Product> spec = Specification.where(null);

        if (category != null && !category.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), category));
        }

        if (supplierId != null) {
            Optional<Supplier> supplier = supplierRepository.findById(supplierId);
            if (supplier.isPresent()) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("supplier"), supplier.get()));
            }
        }

        if (searchQuery != null && !searchQuery.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), "%" + searchQuery.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("sku")), "%" + searchQuery.toLowerCase() + "%")));
        }

        return productRepository.findAll(spec, pageable);
    }

    public Optional<Product> getProductById(UUID id) {
        return productRepository.findById(id);
    }

    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    @Transactional
    public Product createProduct(Product product) {
        // Validate supplier if provided
        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(
                            () -> new RuntimeException("Supplier not found with ID: " + product.getSupplier().getId()));
            product.setSupplier(supplier);
        }

        return productRepository.save(product);
    }

    @Transactional
    public Optional<Product> updateProduct(UUID id, Product product) {
        if (!productRepository.existsById(id)) {
            return Optional.empty();
        }

        // Get the existing product to preserve the supplier
        Product existingProduct = productRepository.findById(id).get();

        // Update fields while preserving the supplier
        existingProduct.setName(product.getName());
        existingProduct.setSku(product.getSku());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setPrice(product.getPrice());

        // Only update supplier if explicitly provided in the request
        if (product.getSupplier() != null && product.getSupplier().getId() != null) {
            Supplier supplier = supplierRepository.findById(product.getSupplier().getId())
                    .orElseThrow(
                            () -> new RuntimeException("Supplier not found with ID: " + product.getSupplier().getId()));
            existingProduct.setSupplier(supplier);
        }

        return Optional.of(productRepository.save(existingProduct));
    }

    @Transactional
    public boolean deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            return false;
        }
        productRepository.deleteById(id);
        return true;
    }
}
