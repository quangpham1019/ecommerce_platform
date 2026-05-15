package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.modules.catalog.domain.ProductVariantInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductVariantInventoryRepository extends JpaRepository<ProductVariantInventory, Long> {
    Optional<ProductVariantInventory> findByProductVariant_Id(Long productVariantId);
}
