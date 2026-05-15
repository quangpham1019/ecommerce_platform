package com.quang.marketplace.modules.seller.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One seller profile per user (MVP constraint)
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "shop_name", nullable = false)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerProfileStatus status = SellerProfileStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SellerProfile() {}

    public SellerProfile(Long userId, String displayName) {
        if (userId == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }

        this.userId = userId;
        this.displayName = displayName;
    }

    // ===== Business Behavior =====

    public void activate() {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalStateException("Display name is required before activation");
        }
        this.status = SellerProfileStatus.ACTIVE;
    }

    public void suspend() {
        this.status = SellerProfileStatus.SUSPENDED;
    }

    public void deactivate() {
        this.status = SellerProfileStatus.DEACTIVATED;
    }

    public void updateProfile(String displayName, String bio) {
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }

        this.displayName = displayName;
        this.bio = bio;
    }

    public boolean isActive() {
        return this.status == SellerProfileStatus.ACTIVE;
    }

    public String getCode() {
        // simple code generation based on display name and id (for SKU prefix)
        // Alo Asian Kitchen -> AAK-{id}
        // Tropical Tea House -> TTH-{id}

        // split by non-alphanumeric, take first char of each word, uppercase, append id
        String alphaNumericDisplayName = displayName.replaceAll("[^A-Z0-9]+", " ").trim().toUpperCase(); // keep only letters and digits
        String[] parts = alphaNumericDisplayName.split("\\s+");
        StringBuilder codeBuilder = new StringBuilder();
        
        for (String part : parts) {
            if (!part.isEmpty()) {
                codeBuilder.append(part.charAt(0));
            }
        }
        return codeBuilder.append("-").append(id).toString();
    }

    // ===== Lifecycle =====

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

    // ===== Getters =====

    public Long getId() { return id; }

    public Long getUserId() { return userId; }

    public String getDisplayName() { return displayName; }

    public String getBio() { return bio; }

    public SellerProfileStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}