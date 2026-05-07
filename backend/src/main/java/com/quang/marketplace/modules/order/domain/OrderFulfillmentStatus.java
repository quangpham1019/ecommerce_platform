package com.quang.marketplace.modules.order.domain;

public enum OrderFulfillmentStatus {
    PENDING,
    CONFIRMED,
    PACKING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED,
    RETURNED
}
