package com.quang.marketplace.modules.cart.infrastructure;

import com.quang.marketplace.modules.cart.domain.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndStatus(Long userId, String status);
    Optional<Cart> findByGuestToken(String guestToken);
}
