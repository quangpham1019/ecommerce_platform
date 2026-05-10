package com.quang.marketplace.modules.seller.api;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;

public record SellerProfileResponse (
    Long id,
    Long userId,
    String displayName,
    String bio,
    SellerProfileStatus status
) {

    public static SellerProfileResponse fromEntity(SellerProfile sellerProfile) {
        return new SellerProfileResponse(
            sellerProfile.getId(),
            sellerProfile.getUserId(),
            sellerProfile.getDisplayName(),
            sellerProfile.getBio(),
            sellerProfile.getStatus()
        );
    }
}