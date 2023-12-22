package com.urcodebin.api.error.exception;

public class InvalidLoginFormatException extends RuntimeException {

    public InvalidLoginFormatException() {
        super("Invalid login format body. Please format your login body properly.");
    }

    public InvalidLoginFormatException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public InvalidLoginFormatException(final String message) {
        super(message);
    }
}
