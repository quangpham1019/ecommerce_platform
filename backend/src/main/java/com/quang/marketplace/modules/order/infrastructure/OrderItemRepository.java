package com.quang.marketplace.modules.order.infrastructure;

import com.quang.marketplace.modules.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findBySellerOrderId(Long sellerOrderId);
}
