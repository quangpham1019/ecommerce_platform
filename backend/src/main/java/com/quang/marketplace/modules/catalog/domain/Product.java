package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_profile_id", nullable = false)
    private Long sellerProfileId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private boolean published = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Product() {}

    public Product(Long sellerProfileId, String title, String description) {
        this.sellerProfileId = sellerProfileId;
        this.title = title;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Long getSellerProfileId() { return sellerProfileId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
}
