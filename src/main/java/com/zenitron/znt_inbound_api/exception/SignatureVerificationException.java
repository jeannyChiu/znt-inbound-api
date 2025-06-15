package com.zenitron.znt_inbound_api.exception;

public class SignatureVerificationException extends RuntimeException {
    public SignatureVerificationException(String message) {
        super(message);
    }
} 