package com.quang.marketplace.modules.seller.api;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import com.quang.marketplace.shared.security.CurrentUserProvider;
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

    record CreateSellerProfileRequest(String displayName) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSellerProfileRequest req) {

        Long userId = currentUserProvider.getCurrentUserId();

        if (req.displayName() == null || req.displayName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "displayName is required");
        }

        if (repo.existsByUserIdAndStatus(userId, "ACTIVE")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active seller profile");
        }

        SellerProfile p = new SellerProfile(userId, req.displayName());
        SellerProfile saved = repo.save(p);

        return ResponseEntity.created(URI.create("/api/seller-profiles/" + saved.getId())).body(saved.getId());
    }
}
