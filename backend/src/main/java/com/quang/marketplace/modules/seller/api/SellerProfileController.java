package com.quang.marketplace.modules.seller.api;

import com.quang.marketplace.modules.seller.application.SellerProfileService;
import com.quang.marketplace.shared.security.CurrentUserProvider;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/seller-profiles")
public class SellerProfileController {

    private final CurrentUserProvider currentUserProvider;
    private final SellerProfileService service;

    public SellerProfileController(CurrentUserProvider currentUserProvider, SellerProfileService service) {
        this.service = service;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSellerProfileRequest req) {

        Long userId = currentUserProvider.getCurrentUserId();

        SellerProfileResponse response = service.createSellerProfile(userId, req);

        return ResponseEntity.created(URI.create("/api/seller-profiles/" + response.id())).body(response);
    }

    @GetMapping("/me")
    public SellerProfileResponse getCurrentSellerProfile() {
        Long userId = currentUserProvider.getCurrentUserId();
        return service.getMySellerProfile(userId);
    }

    @PatchMapping("/me")
    public SellerProfileResponse updateCurrentSellerProfile(@Valid @RequestBody UpdateSellerProfileRequest req) {
        Long userId = currentUserProvider.getCurrentUserId();
       
        return service.updateSellerProfile(userId, req);
    }
}
