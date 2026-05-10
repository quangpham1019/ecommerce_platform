package com.quang.marketplace.modules.seller.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class SellerProfileTest {

    @Test
    public void constructor_validatesArguments() {
        assertThrows(IllegalArgumentException.class, () -> new SellerProfile(null, "Shop"));
        assertThrows(IllegalArgumentException.class, () -> new SellerProfile(1L, ""));
    }

    @Test
    public void updateProfile_validatesDisplayName() {
        SellerProfile p = new SellerProfile(1L, "My Shop");
        assertThrows(IllegalArgumentException.class, () -> p.updateProfile(null, "bio"));
        assertThrows(IllegalArgumentException.class, () -> p.updateProfile("  ", "bio"));
    }

    @Test
    public void activate_and_status_checks() {
        SellerProfile p = new SellerProfile(2L, "Active Shop");
        assertFalse(p.isActive());
        p.activate();
        assertTrue(p.isActive());
        assertEquals(SellerProfileStatus.ACTIVE, p.getStatus());
    }

    @Test
    public void lifecycle_prePersist_and_preUpdate_setTimestamps() throws Exception {
        SellerProfile p = new SellerProfile(3L, "Lifecycle Shop");

        // call package-private lifecycle hook directly (same package)
        p.onCreate();
        assertNotNull(p.getCreatedAt());
        assertNotNull(p.getUpdatedAt());
        Instant firstUpdated = p.getUpdatedAt();

        Thread.sleep(5);
        p.onUpdate();
        assertTrue(p.getUpdatedAt().isAfter(firstUpdated));
    }
}
