package com.quang.marketplace.modules.seller.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quang.marketplace.modules.seller.api.CreateSellerProfileRequest;
import com.quang.marketplace.modules.seller.api.SellerProfileResponse;
import com.quang.marketplace.modules.seller.api.UpdateSellerProfileRequest;
import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.error.ConflictException;
import com.quang.marketplace.shared.error.ResourceNotFoundException;
import com.quang.marketplace.shared.error.UnauthenticatedException;

@Service
public class SellerProfileService {
    
    private final SellerProfileRepository sellerRepo;

    public SellerProfileService(SellerProfileRepository sellerRepo) {
        this.sellerRepo = sellerRepo;
    }

    
    public SellerProfileResponse getMySellerProfile(Long userId) {
        SellerProfile profile = sellerRepo.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));

        return SellerProfileResponse.fromEntity(profile);
    }

    @Transactional
    public SellerProfileResponse createSellerProfile(Long userId, CreateSellerProfileRequest request) {
        
        if (userId == null) {
            throw new UnauthenticatedException();
        }

        // Check if the user already has a seller profile
        if (sellerRepo.existsByUserId(userId)) {
            throw new ConflictException("User already has a seller profile");
        }

        SellerProfile newProfile = new SellerProfile(userId, request.displayName());
        newProfile.updateProfile(request.displayName(), request.bio());
        newProfile.activate(); // For simplicity, we directly activate. In real case, might need approval workflow.

        SellerProfile savedProfile = sellerRepo.save(newProfile);

        return SellerProfileResponse.fromEntity(savedProfile);
    }

    @Transactional
    public SellerProfileResponse updateSellerProfile(Long userId, UpdateSellerProfileRequest req) {
        
        if (userId == null) {
            throw new UnauthenticatedException();
        }

        SellerProfile profile = sellerRepo.findByUserId(userId)
            .filter(SellerProfile::isActive)
            .orElseThrow(() -> new ResourceNotFoundException("Seller profile not found"));
        
        profile.updateProfile(req.displayName(), req.bio());
        SellerProfile updatedProfile = sellerRepo.save(profile);
        
        return SellerProfileResponse.fromEntity(updatedProfile);
    }
}
