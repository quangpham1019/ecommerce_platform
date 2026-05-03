package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.modules.catalog.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByProductVariantId(Long variantId);
}
