package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "variant_images")
public class VariantImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "alt_text")
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected VariantImage() {}

    public VariantImage(ProductVariant productVariant, String imageUrl, String altText, int sortOrder) {
        if (productVariant == null) {
            throw new IllegalArgumentException("ProductVariant is required");
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        if (sortOrder < 0) {
            throw new IllegalArgumentException("Sort order cannot be negative");
        }

        this.productVariant = productVariant;
        this.imageUrl = imageUrl;
        this.altText = altText == null || altText.isBlank() ? null : altText;
        this.sortOrder = sortOrder;
    }

    void assignToVariant(ProductVariant productVariant) {
        this.productVariant = productVariant;
    }

    @PrePersist
    void onCreate() { this.createdAt = Instant.now(); }

    public Long getId() { return id; }
    public ProductVariant getProductVariant() { return productVariant; }
    public String getImageUrl() { return imageUrl; }
    public String getAltText() { return altText; }
    public int getSortOrder() { return sortOrder; }
}
