package com.quang.marketplace.shared.error;

public abstract class ApiException extends RuntimeException {
    protected ApiException(String message) {
        super(message);
    }
}