package com.urcodebin.api.error.exception;

public class MissingRequiredSourceCodeException extends RuntimeException {

    public MissingRequiredSourceCodeException() {
        super();
    }

    public MissingRequiredSourceCodeException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public MissingRequiredSourceCodeException(final String message) {
        super(message);
    }
}
