package com.quang.marketplace.modules.seller.infrastructure;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserIdAndActiveTrue(Long userId);
    boolean existsByUserIdAndActiveTrue(Long userId);
}
