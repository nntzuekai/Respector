package com.urcodebin.api.controllers.requestbody;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupRequestBody {

    @JsonProperty("account_username")
    private String username;

    @JsonProperty("account_email")
    private String email;

    @JsonProperty("account_password")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
