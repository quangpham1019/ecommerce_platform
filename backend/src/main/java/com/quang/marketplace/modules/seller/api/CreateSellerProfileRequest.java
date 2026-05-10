package com.quang.marketplace.modules.seller.api;

import jakarta.validation.constraints.NotBlank;

public record CreateSellerProfileRequest (

    @NotBlank
    String displayName,
    String bio
) {}