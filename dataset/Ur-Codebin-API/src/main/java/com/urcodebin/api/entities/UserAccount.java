package com.urcodebin.api.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue
    @Column(name = "account_id", unique = true, nullable = false)
    private Long id;

    @NotNull
    @NotEmpty
    @Column(name = "account_username")
    private String username;

    @NotNull
    @NotEmpty
    @Column(name = "account_password")
    private String password;

    @NotNull
    @NotEmpty
    @Column(name = "account_email")
    private String email;

    @OneToMany(
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            mappedBy = "userAccount"
    )
    @JsonIgnore
    private List<CodePaste> userCodePastes = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public List<CodePaste> getCodePastes() {
        return new ArrayList<>(userCodePastes);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCodePaste(List<CodePaste> pastes) {
        userCodePastes.addAll(pastes);
    }

    public void setCodePaste(CodePaste paste) {
        userCodePastes.add(paste);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAccount)) return false;
        UserAccount that = (UserAccount) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(password, that.password) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, email);
    }
}
