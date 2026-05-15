package com.quang.marketplace.modules.catalog.infrastructure;

import com.quang.marketplace.modules.catalog.domain.Product;
import com.quang.marketplace.modules.catalog.domain.ProductStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerProfileId(Long sellerProfileId);
    List<Product> findByStatus(ProductStatus status);

    boolean existsBySellerProfileIdAndSlug(Long sellerProfileId, String slug);
}
