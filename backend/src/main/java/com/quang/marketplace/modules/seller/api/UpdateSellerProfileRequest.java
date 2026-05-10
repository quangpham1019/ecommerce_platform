package com.quang.marketplace.modules.seller.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateSellerProfileRequest(
    
    @NotBlank
    String displayName,
    String bio
) {
}
