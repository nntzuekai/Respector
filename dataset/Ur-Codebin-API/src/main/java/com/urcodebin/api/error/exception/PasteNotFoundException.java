package com.urcodebin.api.error.exception;

public class PasteNotFoundException extends RuntimeException {

    public PasteNotFoundException() {
        super();
    }

    public PasteNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public PasteNotFoundException(final String message) {
        super(message);
    }
}
