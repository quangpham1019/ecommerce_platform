package com.quang.marketplace.shared.error;

public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException() {
        super("Email is already registered");
    }
}