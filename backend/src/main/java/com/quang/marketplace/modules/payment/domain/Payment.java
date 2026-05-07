package com.quang.marketplace.modules.payment.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "marketplace_order_id", nullable = false)
    private Long marketplaceOrderId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency_code", nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String currencyCode = "USD";

    @Column(name = "provider", nullable = false)
    private String provider = "MOCK";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "provider_reference")
    private String providerReference;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "authorized_at")
    private Instant authorizedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Payment() {}

    public Payment(
            Long marketplaceOrderId,
            BigDecimal amount,
            String currencyCode,
            String provider
    ) {
        if (marketplaceOrderId == null) {
            throw new IllegalArgumentException("Marketplace order is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("Currency code is required");
        }
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Payment provider is required");
        }

        this.marketplaceOrderId = marketplaceOrderId;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.provider = provider;
    }

    public void markSuccessful(String providerReference) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be marked successful");
        }

        this.status = PaymentStatus.SUCCESSFUL;
        this.providerReference = providerReference;
        this.authorizedAt = Instant.now();
    }

    public void markFailed(String failureReason) {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payments can be marked failed");
        }

        this.status = PaymentStatus.FAILED;
        this.failureReason = failureReason;
        this.failedAt = Instant.now();
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }

    public Long getMarketplaceOrderId() { return marketplaceOrderId; }

    public BigDecimal getAmount() { return amount; }

    public String getCurrencyCode() { return currencyCode; }

    public String getProvider() { return provider; }

    public PaymentStatus getStatus() { return status; }

    public String getProviderReference() { return providerReference; }

    public String getFailureReason() { return failureReason; }

    public Instant getAuthorizedAt() { return authorizedAt; }

    public Instant getFailedAt() { return failedAt; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
}