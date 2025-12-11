package Commerce360.service;

import Commerce360.dto.SupplierProductDTO;
import Commerce360.entity.Product;
import Commerce360.entity.Supplier;
import Commerce360.entity.SupplierProduct;
import Commerce360.repository.ProductRepository;
import Commerce360.repository.SupplierProductRepository;
import Commerce360.repository.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SupplierProductService {

    @Autowired
    private SupplierProductRepository supplierProductRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public SupplierProductDTO addProductToSupplierCatalog(UUID supplierId, UUID productId,
            String supplierSku, BigDecimal supplierPrice,
            Integer minimumOrderQuantity, Integer stockAvailable,
            Integer leadTimeDays, String description, String imageUrl) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if supplier already offers this product
        supplierProductRepository.findBySupplierAndProduct(supplier, product)
                .ifPresent(sp -> {
                    throw new RuntimeException("Supplier already offers this product");
                });

        SupplierProduct supplierProduct = SupplierProduct.builder()
                .supplier(supplier)
                .product(product)
                .supplierSku(supplierSku)
                .supplierPrice(supplierPrice)
                .minimumOrderQuantity(minimumOrderQuantity != null ? minimumOrderQuantity : 1)
                .stockAvailable(stockAvailable != null ? stockAvailable : 0)
                .leadTimeDays(leadTimeDays != null ? leadTimeDays : 7)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .description(description)
                .imageUrl(imageUrl)
                .build();

        supplierProduct = supplierProductRepository.save(supplierProduct);
        return SupplierProductDTO.fromEntity(supplierProduct);
    }

    @Transactional
    public SupplierProductDTO updateSupplierProduct(UUID supplierProductId, BigDecimal supplierPrice,
            Integer stockAvailable, Integer leadTimeDays,
            Boolean isActive, String description, String imageUrl) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(supplierProductId)
                .orElseThrow(() -> new RuntimeException("Supplier product not found"));

        if (supplierPrice != null) {
            supplierProduct.setSupplierPrice(supplierPrice);
        }
        if (stockAvailable != null) {
            supplierProduct.setStockAvailable(stockAvailable);
        }
        if (leadTimeDays != null) {
            supplierProduct.setLeadTimeDays(leadTimeDays);
        }
        if (isActive != null) {
            supplierProduct.setIsActive(isActive);
        }
        if (description != null) {
            supplierProduct.setDescription(description);
        }
        if (imageUrl != null) {
            supplierProduct.setImageUrl(imageUrl);
        }

        supplierProduct.setUpdatedAt(LocalDateTime.now());
        supplierProduct = supplierProductRepository.save(supplierProduct);
        return SupplierProductDTO.fromEntity(supplierProduct);
    }

    public Page<SupplierProductDTO> getSupplierProducts(UUID supplierId, Boolean activeOnly, Pageable pageable) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));

        Page<SupplierProduct> products;
        if (activeOnly != null && activeOnly) {
            products = supplierProductRepository.findBySupplierAndIsActive(supplier, true, pageable);
        } else {
            products = supplierProductRepository.findBySupplier(supplier, pageable);
        }

        return products.map(SupplierProductDTO::fromEntity);
    }

    public Page<SupplierProductDTO> getAllAvailableProducts(Pageable pageable) {
        return supplierProductRepository.findAllAvailableProducts(pageable)
                .map(SupplierProductDTO::fromEntity);
    }

    public SupplierProductDTO getSupplierProduct(UUID supplierProductId) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(supplierProductId)
                .orElseThrow(() -> new RuntimeException("Supplier product not found"));
        return SupplierProductDTO.fromEntity(supplierProduct);
    }

    @Transactional
    public void deleteSupplierProduct(UUID supplierProductId) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(supplierProductId)
                .orElseThrow(() -> new RuntimeException("Supplier product not found"));

        // Soft delete by marking as inactive
        supplierProduct.setIsActive(false);
        supplierProduct.setUpdatedAt(LocalDateTime.now());
        supplierProductRepository.save(supplierProduct);
    }

    @Transactional
    public void updateStock(UUID supplierProductId, Integer quantityChange) {
        SupplierProduct supplierProduct = supplierProductRepository.findById(supplierProductId)
                .orElseThrow(() -> new RuntimeException("Supplier product not found"));

        int newStock = supplierProduct.getStockAvailable() + quantityChange;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock available");
        }

        supplierProduct.setStockAvailable(newStock);
        supplierProduct.setUpdatedAt(LocalDateTime.now());
        supplierProductRepository.save(supplierProduct);
    }
}
