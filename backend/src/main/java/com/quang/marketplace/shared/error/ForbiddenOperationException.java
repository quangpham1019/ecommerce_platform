package com.quang.marketplace.shared.error;

public class ForbiddenOperationException extends ApiException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}