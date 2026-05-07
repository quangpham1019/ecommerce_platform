package com.quang.marketplace.modules.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    //TODO: Validate hierarchy in service layer to prevent cycles (e.g. category cannot be its own parent or ancestor)
    @Column(name = "parent_category_id")
    private Long parentCategoryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    protected Category() {}

    //TODO: Slug should be derived, not passed blindlyly. Consider adding a utility method to generate slug from name and use it in service layer.
    public Category(String name, String slug, Long parentCategoryId) {
        this.name = name;
        this.slug = slug;
        this.parentCategoryId = parentCategoryId;
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
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public Long getParentCategoryId() { return parentCategoryId; }

    public void rename(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
