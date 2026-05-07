package com.quang.marketplace.modules.order.infrastructure;

import com.quang.marketplace.modules.order.domain.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerOrderRepository extends JpaRepository<SellerOrder, Long> {
    List<SellerOrder> findByMarketplaceOrder_Id(Long marketplaceOrderId);
}
