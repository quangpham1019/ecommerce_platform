package com.quang.marketplace.shared.error;

public class BusinessRuleException extends ApiException {
    public BusinessRuleException(String message) {
        super(message);
    }
}