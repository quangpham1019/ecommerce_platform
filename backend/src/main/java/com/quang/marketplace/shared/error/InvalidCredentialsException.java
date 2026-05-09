package com.quang.marketplace.shared.error;

public class InvalidCredentialsException extends ApiException {
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }

    public InvalidCredentialsException(String message) {
        super(message);
    }
}