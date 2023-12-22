package com.urcodebin.api.security.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urcodebin.api.error.exception.AuthorizationHeaderNotFoundException;
import com.urcodebin.api.error.handler.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static com.urcodebin.api.security.SecurityConstants.*;

public class JWTAuthorizationFilter extends BasicAuthenticationFilter {

    public JWTAuthorizationFilter(AuthenticationManager authManager) {
        super(authManager);
    }

    /*
     * Authorization filter is not needed for publicly available endpoints
     * so this method will return TRUE when the URI is a public endpoint
     * and does not need any authorization.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
       String requestURI = request.getRequestURI();
       return requestURI.equals(SIGN_UP_URL) ||
               requestURI.equals(PUBLIC_PASTE_URL);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String token = request.getHeader(HEADER_STRING);

        Authentication authentication;
        try {
            authentication = getAuthentication(token);
        } catch (RuntimeException e) {
            failedAuthorization(e, response);
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken getAuthentication(String token) throws RuntimeException {
        if(token == null)
            throw new AuthorizationHeaderNotFoundException();
        else if(!token.startsWith(TOKEN_PREFIX))
            throw new BadCredentialsException("Authentication header 'Bearer ' prefix not found.");

        String user = JWT.require(Algorithm.HMAC512(SECRET.getBytes()))
                .build()
                .verify(token.replace(TOKEN_PREFIX, ""))
                .getSubject();

        if(user == null)
            return null;
        else
            return new UsernamePasswordAuthenticationToken(user, null, new ArrayList<>());

    }

    private void failedAuthorization(RuntimeException e, HttpServletResponse response) throws IOException {
        ErrorResponse authorizationError = new ErrorResponse(e);
        authorizationError.setStatus(HttpStatus.UNAUTHORIZED.value());
        authorizationError.setError(HttpStatus.UNAUTHORIZED.getReasonPhrase());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(authorizationError.getStatus());
        response.getWriter().write(errorResponseToJson(authorizationError));
    }

    private String errorResponseToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
    }
}
