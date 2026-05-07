package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;

@Entity
@Table(
    name = "product_categories",
    uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "category_id"})
)
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    protected ProductCategory() {}

    public ProductCategory(Long productId, Long categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public Long getCategoryId() { return categoryId; }
}
