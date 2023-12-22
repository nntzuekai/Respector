package com.urcodebin.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserAccountDTO {

    @JsonProperty("account_id")
    private Long id;

    @JsonProperty("account_username")
    private String username;

    @JsonProperty("account_email")
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
