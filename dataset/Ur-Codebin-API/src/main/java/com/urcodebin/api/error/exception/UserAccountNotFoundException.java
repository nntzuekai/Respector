package com.urcodebin.api.error.exception;

public class UserAccountNotFoundException extends RuntimeException {

    public UserAccountNotFoundException() {
        super();
    }

    public UserAccountNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public UserAccountNotFoundException(final String message) {
        super(message);
    }
}
