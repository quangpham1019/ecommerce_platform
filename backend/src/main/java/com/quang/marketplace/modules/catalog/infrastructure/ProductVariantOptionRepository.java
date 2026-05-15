package com.quang.marketplace.modules.catalog.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import com.quang.marketplace.modules.catalog.domain.ProductVariantOption;

public interface ProductVariantOptionRepository extends JpaRepository<ProductVariantOption, Long> {
    
}
