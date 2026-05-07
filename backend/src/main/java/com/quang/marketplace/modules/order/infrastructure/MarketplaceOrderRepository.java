package com.quang.marketplace.modules.order.infrastructure;

import com.quang.marketplace.modules.order.domain.MarketplaceOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MarketplaceOrderRepository extends JpaRepository<MarketplaceOrder, Long> {
    Optional<MarketplaceOrder> findByOrderNumber(String orderNumber);
}
