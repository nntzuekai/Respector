package com.urcodebin.api.services.interfaces;

import com.urcodebin.api.controllers.requestbody.SignupRequestBody;
import com.urcodebin.api.entities.UserAccount;

import java.util.Optional;

public interface UserAccountService {

    Optional<UserAccount> getUserAccountById(Long accountId);

    Optional<UserAccount> findByUsername(String username);

    UserAccount signupNewUserAccount(SignupRequestBody signupAccount);

    boolean isAccountEmailTaken(String email);

    boolean isAccountUsernameTaken(String username);
}
