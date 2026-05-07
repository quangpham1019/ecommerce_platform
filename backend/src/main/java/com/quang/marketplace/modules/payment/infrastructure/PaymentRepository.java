package com.quang.marketplace.modules.payment.infrastructure;

import com.quang.marketplace.modules.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByMarketplaceOrderId(Long marketplaceOrderId);
}
