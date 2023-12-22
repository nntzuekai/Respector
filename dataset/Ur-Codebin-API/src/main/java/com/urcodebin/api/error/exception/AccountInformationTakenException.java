package com.urcodebin.api.error.exception;

public class AccountInformationTakenException extends RuntimeException {

    public AccountInformationTakenException() {
        super();
    }

    public AccountInformationTakenException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public AccountInformationTakenException(final String message) {
        super(message);
    }
}
