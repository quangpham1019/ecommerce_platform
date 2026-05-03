package com.quang.marketplace.modules.seller.api;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.infrastructure.SellerProfileRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.HttpStatus;

import java.net.URI;

@RestController
@RequestMapping("/api/seller-profiles")
public class SellerProfileController {

    private final SellerProfileRepository repo;

    public SellerProfileController(SellerProfileRepository repo) {
        this.repo = repo;
    }

    record CreateSellerProfileRequest(String displayName) {}

    @PostMapping
    public ResponseEntity<?> create(@RequestHeader("X-User-Id") Long userId,
                                    @RequestBody CreateSellerProfileRequest req) {

        if (req.displayName() == null || req.displayName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "displayName is required");
        }

        if (repo.existsByUserIdAndActiveTrue(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has an active seller profile");
        }

        SellerProfile p = new SellerProfile(userId, req.displayName());
        SellerProfile saved = repo.save(p);

        return ResponseEntity.created(URI.create("/api/seller-profiles/" + saved.getId())).body(saved.getId());
    }
}
