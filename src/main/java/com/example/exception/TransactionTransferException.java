package com.example.exception;

public class TransactionTransferException extends RuntimeException {
    public TransactionTransferException(String message) {
        super(message);
    }
}
