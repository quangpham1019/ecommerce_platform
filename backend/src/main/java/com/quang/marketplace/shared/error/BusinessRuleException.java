package com.quang.marketplace.shared.error;

public class BusinessRuleException extends ConflictException {
    public BusinessRuleException(String message) {
        super(message);
    }
}