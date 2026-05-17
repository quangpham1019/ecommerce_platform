package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.modules.catalog.domain.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findBySku(String sku);
    boolean existsByProductIdAndSkuIgnoreCase(Long productId, String sku);
}
