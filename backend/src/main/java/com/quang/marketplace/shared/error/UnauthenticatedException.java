package com.quang.marketplace.shared.error;

public class UnauthenticatedException extends RuntimeException {
    public UnauthenticatedException() {
        super("Authentication is required");
    }
}