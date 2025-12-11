package Commerce360.repository;

import Commerce360.entity.Product;
import Commerce360.entity.Supplier;
import Commerce360.entity.SupplierProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierProductRepository extends JpaRepository<SupplierProduct, UUID> {
    Page<SupplierProduct> findBySupplier(Supplier supplier, Pageable pageable);
    Page<SupplierProduct> findBySupplierAndIsActive(Supplier supplier, Boolean isActive, Pageable pageable);
    
    Optional<SupplierProduct> findBySupplierAndProduct(Supplier supplier, Product product);
    Optional<SupplierProduct> findBySupplierSku(String supplierSku);
    
    List<SupplierProduct> findByProduct(Product product);
    
    @Query("SELECT sp FROM SupplierProduct sp WHERE sp.supplier = :supplier AND sp.isActive = true AND sp.stockAvailable > 0")
    Page<SupplierProduct> findAvailableProductsBySupplier(@Param("supplier") Supplier supplier, Pageable pageable);
    
    @Query("SELECT sp FROM SupplierProduct sp WHERE sp.isActive = true AND sp.stockAvailable > 0")
    Page<SupplierProduct> findAllAvailableProducts(Pageable pageable);
}
