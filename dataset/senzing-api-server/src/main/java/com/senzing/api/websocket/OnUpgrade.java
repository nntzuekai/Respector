package com.senzing.api.websocket;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.METHOD;

@Retention(value=RUNTIME)
@Target(value=METHOD)
public @interface OnUpgrade {

}
