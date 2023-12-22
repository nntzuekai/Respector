package com.urcodebin.api.error.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthorizationHeaderNotFoundException extends AuthenticationException {
    public AuthorizationHeaderNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }

    public AuthorizationHeaderNotFoundException(String msg) {
        super(msg);
    }

    public AuthorizationHeaderNotFoundException() {
        super("Required 'Authorization' token header not found");
    }
}
