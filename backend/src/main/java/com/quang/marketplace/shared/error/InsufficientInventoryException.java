package com.quang.marketplace.shared.error;

public class InsufficientInventoryException extends ConflictException {
    public InsufficientInventoryException(String message) {
        super(message);
    }
}