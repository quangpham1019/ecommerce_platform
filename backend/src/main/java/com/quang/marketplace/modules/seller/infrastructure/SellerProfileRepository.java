package com.quang.marketplace.modules.seller.infrastructure;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import com.quang.marketplace.modules.seller.domain.SellerProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);
    Optional<SellerProfile> findByUserIdAndStatus(Long userId, SellerProfileStatus status);
    boolean existsByUserIdAndStatus(Long userId, SellerProfileStatus status);
}
