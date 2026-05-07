package com.quang.marketplace.modules.catalog.domain;

import java.time.Instant;
import jakarta.persistence.*;
@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ProductImage() {}

    public ProductImage(String imageUrl, String altText, int sortOrder) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        if (sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }

        this.imageUrl = imageUrl;
        this.altText = (altText == null || altText.isBlank()) ? null : altText;
        this.sortOrder = sortOrder;
    }

    void assignToProduct(Product product) {
        this.product = product;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }
    public int getSortOrder() { return sortOrder; }
}