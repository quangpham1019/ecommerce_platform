package com.quang.marketplace.shared.error;

public class ValidationException extends ApiException {
    public ValidationException(String message) {
        super(message);
    }
}