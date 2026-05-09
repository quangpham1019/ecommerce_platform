package com.quang.marketplace.modules.seller.api;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.security.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.net.URI;

@RestController
@RequestMapping("/api/seller-profiles")
public class SellerProfileController {

    private final SellerProfileRepository repo;
    private final CurrentUserProvider currentUserProvider;

    public SellerProfileController(SellerProfileRepository repo, CurrentUserProvider currentUserProvider) {
        this.repo = repo;
        this.currentUserProvider = currentUserProvider;
    }

    record CreateSellerProfileRequest(@NotBlank String displayName, String bio) {}
    record UpdateSellerProfileRequest(@NotBlank String displayName, String bio) {}
    record SellerProfileResponse(Long id, String displayName, String bio, String status) {}

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSellerProfileRequest req) {

        Long userId = currentUserProvider.getCurrentUserId();

        repo.findByUserId(userId)
                .filter(SellerProfile::isActive)
                .ifPresent(profile -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active seller profile");
                });

        SellerProfile p = new SellerProfile(userId, req.displayName());
        p.updateProfile(req.displayName(), req.bio());
        p.activate();
        SellerProfile saved = repo.save(p);

        return ResponseEntity.created(URI.create("/api/seller-profiles/" + saved.getId())).body(saved.getId());
    }

    @GetMapping("/me")
    public SellerProfileResponse getCurrentSellerProfile() {
        Long userId = currentUserProvider.getCurrentUserId();
        SellerProfile profile = repo.findByUserId(userId)
                .filter(SellerProfile::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));

        return new SellerProfileResponse(profile.getId(), profile.getDisplayName(), profile.getBio(), profile.getStatus().name());
    }

    @PatchMapping("/me")
    public SellerProfileResponse updateCurrentSellerProfile(@Valid @RequestBody UpdateSellerProfileRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
        SellerProfile profile = repo.findByUserId(userId)
                .filter(SellerProfile::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));

        profile.updateProfile(req.displayName(), req.bio());
        SellerProfile updated = repo.save(profile);

        return new SellerProfileResponse(updated.getId(), updated.getDisplayName(), updated.getBio(), updated.getStatus().name());
    }
}
