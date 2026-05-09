package com.quang.marketplace.shared.error;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message);
    }
}