package com.urcodebin.api.security.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urcodebin.api.error.handler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FailedAuthenticationHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException e) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(e);
        if(e instanceof PreAuthenticatedCredentialsNotFoundException) {
            updateErrorResponse(errorResponse, HttpStatus.BAD_REQUEST);
        } else {
            updateErrorResponse(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorResponse.getStatus());
        response.getWriter().write(errorResponseToJson(errorResponse));
    }

    private void updateErrorResponse(ErrorResponse error, HttpStatus status) {
        error.setStatus(status.value());
        error.setError(status.getReasonPhrase());
    }

    private String errorResponseToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
