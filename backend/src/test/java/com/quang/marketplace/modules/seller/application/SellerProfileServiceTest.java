package com.quang.marketplace.modules.seller.application;

import com.quang.marketplace.modules.seller.api.CreateSellerProfileRequest;
import com.quang.marketplace.modules.seller.api.SellerProfileResponse;
import com.quang.marketplace.modules.seller.api.UpdateSellerProfileRequest;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.error.ConflictException;
import com.quang.marketplace.shared.error.ResourceNotFoundException;
import com.quang.marketplace.shared.error.UnauthenticatedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerProfileServiceTest {

    @Mock
    SellerProfileRepository sellerRepo;

    @InjectMocks
    SellerProfileService service;

    @Test
    public void create_unauthenticated_throws() {
        CreateSellerProfileRequest req = new CreateSellerProfileRequest("Shop","bio");
        assertThrows(UnauthenticatedException.class, () -> service.createSellerProfile(null, req));
    }

    @Test
    public void create_conflict_when_active_exists() {
        when(sellerRepo.existsByUserId(1L)).thenReturn(true);
        CreateSellerProfileRequest req = new CreateSellerProfileRequest("Shop","bio");
        assertThrows(ConflictException.class, () -> service.createSellerProfile(1L, req));
    }

    @Test
    public void create_saves_and_returns_response() {
        CreateSellerProfileRequest req = new CreateSellerProfileRequest("My Shop","desc");
        SellerProfile saved = new SellerProfile(5L, "My Shop");
        saved.activate();
        when(sellerRepo.existsByUserId(5L)).thenReturn(false);
        when(sellerRepo.save(any(SellerProfile.class))).thenReturn(saved);

        SellerProfileResponse resp = service.createSellerProfile(5L, req);

        assertNotNull(resp);
        assertEquals("My Shop", resp.displayName());
        verify(sellerRepo).save(any(SellerProfile.class));
    }

    @Test
    public void update_missing_profile_throws() {
        when(sellerRepo.findByUserId(9L)).thenReturn(Optional.empty());
        UpdateSellerProfileRequest req = new UpdateSellerProfileRequest("X","bio");
        assertThrows(ResourceNotFoundException.class, () -> service.updateSellerProfile(9L, req));
    }
}
