package com.quang.marketplace.modules.order.infrastructure;

import com.quang.marketplace.modules.order.domain.OrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderFulfillmentRepository extends JpaRepository<OrderFulfillment, Long> {
    Optional<OrderFulfillment> findBySellerOrderId(Long sellerOrderId);
}
