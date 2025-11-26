package com.example.exception;

public class CardNumberGenerationException extends RuntimeException {
    public CardNumberGenerationException(String message) {
        super(message);
    }
}
