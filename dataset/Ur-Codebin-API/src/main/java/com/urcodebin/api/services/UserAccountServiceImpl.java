package com.urcodebin.api.services;

import com.urcodebin.api.controllers.requestbody.SignupRequestBody;
import com.urcodebin.api.entities.UserAccount;
import com.urcodebin.api.repository.UserAccountRepository;
import com.urcodebin.api.services.interfaces.UserAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("AccountService")
public class UserAccountServiceImpl implements UserAccountService {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserAccountServiceImpl(UserAccountRepository userAccountRepository,
                                  PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<UserAccount> getUserAccountById(Long accountId) {
        LOGGER.info("Finding UserAccount with ID: {}", accountId);
        return userAccountRepository.findById(accountId);
    }

    @Override
    public Optional<UserAccount> findByUsername(String username) {
        LOGGER.info("Finding UserAccount with Username: {}", username);
        return userAccountRepository.findByUsername(username);
    }

    @Override
    public UserAccount signupNewUserAccount(SignupRequestBody signupAccount) {
        LOGGER.info("Signing up new account.");
        UserAccount createdAccount = createUserAccountFromSignupBody(signupAccount);
        return userAccountRepository.save(createdAccount);
    }

    @Override
    public boolean isAccountEmailTaken(String email) {
        LOGGER.info("Is Account with Email: {} in use.", email);
        return userAccountRepository.existsByEmail(email);
    }

    @Override
    public boolean isAccountUsernameTaken(String username) {
        LOGGER.info("Is Account with Username: {} in use", username);
        return userAccountRepository.existsByUsername(username);
    }

    private UserAccount createUserAccountFromSignupBody(SignupRequestBody signupBody) {
        UserAccount userAccount = new UserAccount();
        userAccount.setEmail(signupBody.getEmail());
        userAccount.setUsername(signupBody.getUsername());
        userAccount.setPassword(passwordEncoder.encode(signupBody.getPassword()));
        return userAccount;
    }
}
