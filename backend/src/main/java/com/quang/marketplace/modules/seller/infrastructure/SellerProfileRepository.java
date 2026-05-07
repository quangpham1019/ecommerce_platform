package com.quang.marketplace.modules.seller.infrastructure;

import com.quang.marketplace.modules.seller.domain.SellerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerProfileRepository extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserIdAndStatus(Long userId, String status);
    boolean existsByUserIdAndStatus(Long userId, String status);
}
