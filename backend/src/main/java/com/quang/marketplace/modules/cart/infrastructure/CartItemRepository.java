package com.quang.marketplace.modules.cart.infrastructure;

import com.quang.marketplace.modules.cart.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
}
