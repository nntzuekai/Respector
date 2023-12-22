package com.urcodebin.api.services;

import com.urcodebin.api.entities.UserAccount;
import com.urcodebin.api.services.interfaces.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserAccountService userAccountService;

    @Autowired
    public UserDetailsServiceImpl(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserAccount> foundAccount = userAccountService.findByUsername(username);
        if(foundAccount.isEmpty()) {
            throw new UsernameNotFoundException(username);
        }
        final UserAccount account = foundAccount.get();
        return new User(account.getUsername(), account.getPassword(), new ArrayList<>());
    }
}
